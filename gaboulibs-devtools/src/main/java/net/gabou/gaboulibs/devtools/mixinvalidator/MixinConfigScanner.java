package net.gabou.gaboulibs.devtools.mixinvalidator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class MixinConfigScanner {
    private final Path projectDir;
    private final List<Path> resourceDirs;

    public MixinConfigScanner(Path projectDir, Collection<Path> resourceDirs) {
        this.projectDir = projectDir.toAbsolutePath().normalize();
        this.resourceDirs = normalizePaths(resourceDirs);
    }

    public ValidatorConfig readValidatorConfig(Path configFile, ValidationReport report) {
        Path normalizedConfig = resolve(configFile);
        if (!Files.isRegularFile(normalizedConfig)) {
            report.error(
                    normalizedConfig.toString(),
                    "Mixin validator config file does not exist.",
                    "Create mixin-validator.json or configure gabouMixinValidator.configFile."
            );
            return ValidatorConfig.empty(normalizedConfig);
        }

        try (Reader reader = Files.newBufferedReader(normalizedConfig, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            String modId = stringOrEmpty(root, "modId");
            List<String> mixinConfigs = readStringArray(root, "mixinConfigs");
            List<OptionalModTargetRules.Rule> optionalTargets = readOptionalTargets(root, report, normalizedConfig);

            if (modId.isEmpty()) {
                report.warning(
                        normalizedConfig.toString(),
                        "Validator config is missing modId.",
                        "Set modId so reports can identify the owning mod."
                );
            }

            if (mixinConfigs.isEmpty()) {
                report.warning(
                        normalizedConfig.toString(),
                        "Validator config does not list any mixinConfigs.",
                        "Add mixin config json files or remove the validateMixins task for this project."
                );
            }

            return new ValidatorConfig(normalizedConfig, modId, mixinConfigs, optionalTargets);
        } catch (IOException | IllegalStateException | JsonSyntaxException ex) {
            report.error(
                    normalizedConfig.toString(),
                    "Could not read mixin validator config: " + ex.getMessage(),
                    "Fix the JSON syntax and make sure the file is readable."
            );
            return ValidatorConfig.empty(normalizedConfig);
        }
    }

    public List<MixinConfigModel> scanMixinConfigs(ValidatorConfig validatorConfig, ValidationReport report) {
        List<MixinConfigModel> models = new ArrayList<>();
        Set<Path> seen = new LinkedHashSet<>();

        for (String mixinConfigName : validatorConfig.getMixinConfigs()) {
            Path configPath = locateMixinConfig(mixinConfigName);
            if (configPath == null) {
                report.error(
                        mixinConfigName,
                        "Mixin config listed in mixin-validator.json was not found.",
                        "Place the file in src/main/resources or update mixin-validator.json."
                );
                continue;
            }
            if (seen.add(configPath)) {
                MixinConfigModel model = parseMixinConfig(configPath, report);
                if (model != null) {
                    models.add(model);
                }
            }
        }

        return models;
    }

    private MixinConfigModel parseMixinConfig(Path configPath, ValidationReport report) {
        try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            boolean required = booleanOrDefault(root, "required", false);
            String packageName = stringOrEmpty(root, "package");
            String pluginClass = stringOrEmpty(root, "plugin");

            if (packageName.isEmpty()) {
                report.warning(
                        configPath.toString(),
                        "Mixin config does not declare a package.",
                        "Declare the mixin package or use fully qualified mixin class names."
                );
            }

            return new MixinConfigModel(
                    configPath,
                    required,
                    packageName,
                    pluginClass,
                    readStringArray(root, "mixins"),
                    readStringArray(root, "client"),
                    readStringArray(root, "server")
            );
        } catch (IOException | IllegalStateException | JsonSyntaxException ex) {
            report.error(
                    configPath.toString(),
                    "Could not read mixin config: " + ex.getMessage(),
                    "Fix the mixin JSON file."
            );
            return null;
        }
    }

    private Path locateMixinConfig(String mixinConfigName) {
        Path direct = resolve(Path.of(mixinConfigName));
        if (Files.isRegularFile(direct)) {
            return direct;
        }

        for (Path resourceDir : resourceDirs) {
            Path candidate = resourceDir.resolve(mixinConfigName).normalize();
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
        }

        try (Stream<Path> stream = Files.walk(projectDir, 6)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals(mixinConfigName))
                    .filter(path -> !path.toString().contains("\\build\\") && !path.toString().contains("/build/"))
                    .findFirst()
                    .orElse(null);
        } catch (IOException ignored) {
            return null;
        }
    }

    private List<OptionalModTargetRules.Rule> readOptionalTargets(JsonObject root, ValidationReport report, Path configFile) {
        JsonArray array = arrayOrEmpty(root, "optionalTargets");
        List<OptionalModTargetRules.Rule> rules = new ArrayList<>();
        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                report.warning(configFile.toString(), "Ignoring malformed optionalTargets entry.", "Use objects with classPrefix and requiredModId.");
                continue;
            }
            JsonObject object = element.getAsJsonObject();
            OptionalModTargetRules.Rule rule = new OptionalModTargetRules.Rule(
                    stringOrEmpty(object, "classPrefix"),
                    stringOrEmpty(object, "requiredModId")
            );
            if (!rule.isValid()) {
                report.warning(configFile.toString(), "Ignoring incomplete optionalTargets entry.", "Set both classPrefix and requiredModId.");
                continue;
            }
            rules.add(rule);
        }
        return rules;
    }

    private static List<String> readStringArray(JsonObject root, String name) {
        JsonArray array = arrayOrEmpty(root, name);
        List<String> values = new ArrayList<>();
        for (JsonElement element : array) {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                String value = element.getAsString().trim();
                if (!value.isEmpty()) {
                    values.add(value);
                }
            }
        }
        return values;
    }

    private static JsonArray arrayOrEmpty(JsonObject root, String name) {
        JsonElement element = root.get(name);
        if (element == null || !element.isJsonArray()) {
            return new JsonArray();
        }
        return element.getAsJsonArray();
    }

    private static String stringOrEmpty(JsonObject root, String name) {
        JsonElement element = root.get(name);
        if (element == null || !element.isJsonPrimitive()) {
            return "";
        }
        try {
            return element.getAsString().trim();
        } catch (ClassCastException ignored) {
            return "";
        }
    }

    private static boolean booleanOrDefault(JsonObject root, String name, boolean fallback) {
        JsonElement element = root.get(name);
        if (element == null || !element.isJsonPrimitive()) {
            return fallback;
        }
        try {
            return element.getAsBoolean();
        } catch (ClassCastException | IllegalStateException ignored) {
            return fallback;
        }
    }

    private Path resolve(Path path) {
        if (path.isAbsolute()) {
            return path.normalize();
        }
        return projectDir.resolve(path).normalize();
    }

    private static List<Path> normalizePaths(Collection<Path> paths) {
        if (paths == null || paths.isEmpty()) {
            return Collections.emptyList();
        }
        List<Path> normalized = new ArrayList<>();
        for (Path path : paths) {
            if (path != null) {
                normalized.add(path.toAbsolutePath().normalize());
            }
        }
        return normalized;
    }

    public static final class ValidatorConfig {
        private final Path file;
        private final String modId;
        private final List<String> mixinConfigs;
        private final List<OptionalModTargetRules.Rule> optionalTargets;

        private ValidatorConfig(Path file, String modId, List<String> mixinConfigs, List<OptionalModTargetRules.Rule> optionalTargets) {
            this.file = file;
            this.modId = modId == null ? "" : modId;
            this.mixinConfigs = Collections.unmodifiableList(new ArrayList<>(mixinConfigs));
            this.optionalTargets = Collections.unmodifiableList(new ArrayList<>(optionalTargets));
        }

        public static ValidatorConfig empty(Path file) {
            return new ValidatorConfig(file, "", Collections.emptyList(), Collections.emptyList());
        }

        public Path getFile() {
            return file;
        }

        public String getModId() {
            return modId;
        }

        public List<String> getMixinConfigs() {
            return mixinConfigs;
        }

        public OptionalModTargetRules getOptionalTargetRules() {
            return new OptionalModTargetRules(optionalTargets);
        }
    }
}
