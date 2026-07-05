package net.gabou.gaboulibs.devtools.mixinvalidator;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class MixinValidatorMain {
    private MixinValidatorMain() {
    }

    public static void main(String[] args) {
        ValidationOptions options = ValidationOptions.fromArgs(args);
        ValidationReport report = validate(options);
        report.print(System.out);
        if (shouldFail(report, options.isFailOnError(), options.isFailOnWarning())) {
            System.exit(1);
        }
    }

    public static ValidationReport validate(ValidationOptions options) {
        ValidationReport report = new ValidationReport();
        MixinConfigScanner scanner = new MixinConfigScanner(options.getProjectDir(), options.getResourceDirs());
        MixinConfigScanner.ValidatorConfig validatorConfig = scanner.readValidatorConfig(options.getConfigFile(), report);
        List<MixinConfigModel> mixinConfigs = scanner.scanMixinConfigs(validatorConfig, report);

        OptionalModTargetRules optionalTargetRules = validatorConfig.getOptionalTargetRules();
        TargetResolver targetResolver = new TargetResolver(options.getClasspath());
        MixinClassAnalyzer mixinClassAnalyzer = new MixinClassAnalyzer();

        for (MixinConfigModel mixinConfig : mixinConfigs) {
            validateMixinConfig(mixinConfig, targetResolver, mixinClassAnalyzer, optionalTargetRules, options.getSourceDirs(), report);
        }

        return report;
    }

    public static boolean shouldFail(ValidationReport report, boolean failOnError, boolean failOnWarning) {
        if (failOnWarning && (report.hasErrors() || report.hasWarnings())) {
            return true;
        }
        return failOnError && report.hasErrors();
    }

    private static void validateMixinConfig(
            MixinConfigModel mixinConfig,
            TargetResolver targetResolver,
            MixinClassAnalyzer mixinClassAnalyzer,
            OptionalModTargetRules optionalTargetRules,
            Collection<Path> sourceDirs,
            ValidationReport report
    ) {
        for (String mixinClassName : mixinConfig.getDeclaredMixinClasses()) {
            Optional<byte[]> mixinBytes = targetResolver.readClassBytes(mixinClassName);
            if (mixinBytes.isEmpty()) {
                report.error(
                        mixinConfig.describe() + " -> " + mixinClassName,
                        "Mixin class was not found on the validation classpath.",
                        sourceExists(sourceDirs, mixinClassName)
                                ? "The source file exists, but compiled output is missing. Make validateMixins depend on classes."
                                : "Fix the mixin package/name or make sure the source set is on the task classpath."
                );
                continue;
            }

            MixinClassAnalyzer.AnalysisResult analysis = mixinClassAnalyzer.analyze(mixinClassName, mixinBytes.get(), report);
            for (String targetClassName : analysis.getTargetClassNames()) {
                validateTarget(mixinConfig, analysis, targetClassName, targetResolver, optionalTargetRules, report);
            }
        }
    }

    private static void validateTarget(
            MixinConfigModel mixinConfig,
            MixinClassAnalyzer.AnalysisResult analysis,
            String targetClassName,
            TargetResolver targetResolver,
            OptionalModTargetRules optionalTargetRules,
            ValidationReport report
    ) {
        Optional<OptionalModTargetRules.Rule> optionalRule = optionalTargetRules.findForTarget(targetClassName);
        boolean targetExists = targetResolver.classExists(targetClassName);
        String location = analysis.getMixinClassName() + " -> " + targetClassName;

        if (optionalRule.isPresent()) {
            OptionalModTargetRules.Rule rule = optionalRule.get();
            if (mixinConfig.isRequired()) {
                report.error(
                        location,
                        "Mixin targets optional mod '" + rule.getRequiredModId() + "' from a required mixin config.",
                        "Move this mixin to an optional/compat mixin config with required=false."
                );
            }

            if (!mixinConfig.hasPluginClass() && !analysis.hasPseudoAnnotation()) {
                report.warning(
                        location,
                        "Optional mod target is not visibly gated by a mixin plugin or @Pseudo.",
                        "Gate this mixin on mod '" + rule.getRequiredModId() + "' or add @Pseudo if the missing target is intentional."
                );
            }

            if (!targetExists) {
                report.warning(
                        location,
                        "Optional target class is not present on the validation classpath.",
                        "This is acceptable only when the mixin is gated on mod '" + rule.getRequiredModId() + "'."
                );
            }
            return;
        }

        if (!targetExists) {
            report.error(
                    location,
                    "Target class was not found on the validation classpath.",
                    "Add the dependency, fix the target class name, or add an optionalTargets rule if this belongs to an optional mod."
            );
        }
    }

    private static boolean sourceExists(Collection<Path> sourceDirs, String className) {
        String relativePath = className.replace('.', File.separatorChar) + ".java";
        for (Path sourceDir : sourceDirs) {
            if (Files.isRegularFile(sourceDir.resolve(relativePath))) {
                return true;
            }
        }
        return false;
    }

    public static final class ValidationOptions {
        private Path projectDir = Path.of(".").toAbsolutePath().normalize();
        private Path configFile = projectDir.resolve("mixin-validator.json");
        private List<Path> classpath = Collections.emptyList();
        private List<Path> resourceDirs = Collections.emptyList();
        private List<Path> sourceDirs = Collections.emptyList();
        private boolean failOnError = true;
        private boolean failOnWarning = false;

        public static ValidationOptions fromArgs(String[] args) {
            ValidationOptions options = new ValidationOptions();
            for (int index = 0; index < args.length; index++) {
                String arg = args[index];
                if (!arg.startsWith("--")) {
                    continue;
                }
                String value = index + 1 < args.length ? args[++index] : "";
                switch (arg) {
                    case "--project-dir":
                        options.setProjectDir(Path.of(value));
                        break;
                    case "--config":
                        options.setConfigFile(Path.of(value));
                        break;
                    case "--classpath":
                        options.setClasspath(splitPathList(value));
                        break;
                    case "--resources":
                        options.setResourceDirs(splitPathList(value));
                        break;
                    case "--sources":
                        options.setSourceDirs(splitPathList(value));
                        break;
                    case "--fail-on-error":
                        options.setFailOnError(Boolean.parseBoolean(value));
                        break;
                    case "--fail-on-warning":
                        options.setFailOnWarning(Boolean.parseBoolean(value));
                        break;
                    default:
                        break;
                }
            }
            return options;
        }

        public Path getProjectDir() {
            return projectDir;
        }

        public void setProjectDir(Path projectDir) {
            this.projectDir = projectDir.toAbsolutePath().normalize();
        }

        public Path getConfigFile() {
            return configFile;
        }

        public void setConfigFile(Path configFile) {
            this.configFile = configFile.toAbsolutePath().normalize();
        }

        public List<Path> getClasspath() {
            return classpath;
        }

        public void setClasspath(Collection<Path> classpath) {
            this.classpath = immutablePaths(classpath);
        }

        public List<Path> getResourceDirs() {
            return resourceDirs;
        }

        public void setResourceDirs(Collection<Path> resourceDirs) {
            this.resourceDirs = immutablePaths(resourceDirs);
        }

        public List<Path> getSourceDirs() {
            return sourceDirs;
        }

        public void setSourceDirs(Collection<Path> sourceDirs) {
            this.sourceDirs = immutablePaths(sourceDirs);
        }

        public boolean isFailOnError() {
            return failOnError;
        }

        public void setFailOnError(boolean failOnError) {
            this.failOnError = failOnError;
        }

        public boolean isFailOnWarning() {
            return failOnWarning;
        }

        public void setFailOnWarning(boolean failOnWarning) {
            this.failOnWarning = failOnWarning;
        }

        private static List<Path> splitPathList(String value) {
            if (value == null || value.trim().isEmpty()) {
                return Collections.emptyList();
            }
            String[] parts = value.split(java.util.regex.Pattern.quote(File.pathSeparator));
            List<Path> paths = new ArrayList<>();
            for (String part : parts) {
                if (!part.trim().isEmpty()) {
                    paths.add(Path.of(part));
                }
            }
            return paths;
        }

        private static List<Path> immutablePaths(Collection<Path> paths) {
            if (paths == null || paths.isEmpty()) {
                return Collections.emptyList();
            }
            List<Path> copy = new ArrayList<>();
            for (Path path : paths) {
                if (path != null) {
                    copy.add(path.toAbsolutePath().normalize());
                }
            }
            return Collections.unmodifiableList(copy);
        }
    }
}
