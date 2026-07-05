package net.gabou.gaboulibs.devtools.mixinvalidator;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MixinConfigModel {
    private final Path file;
    private final boolean required;
    private final String packageName;
    private final String pluginClass;
    private final List<String> mixins;
    private final List<String> clientMixins;
    private final List<String> serverMixins;

    public MixinConfigModel(
            Path file,
            boolean required,
            String packageName,
            String pluginClass,
            List<String> mixins,
            List<String> clientMixins,
            List<String> serverMixins
    ) {
        this.file = file;
        this.required = required;
        this.packageName = normalize(packageName);
        this.pluginClass = normalize(pluginClass);
        this.mixins = immutableCopy(mixins);
        this.clientMixins = immutableCopy(clientMixins);
        this.serverMixins = immutableCopy(serverMixins);
    }

    public Path getFile() {
        return file;
    }

    public boolean isRequired() {
        return required;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getPluginClass() {
        return pluginClass;
    }

    public boolean hasPluginClass() {
        return !pluginClass.isEmpty();
    }

    public List<String> getDeclaredMixinClasses() {
        List<String> all = new ArrayList<>();
        addQualified(all, mixins);
        addQualified(all, clientMixins);
        addQualified(all, serverMixins);
        return Collections.unmodifiableList(all);
    }

    public String describe() {
        return file == null ? "<unknown mixin config>" : file.toString();
    }

    private void addQualified(List<String> out, List<String> entries) {
        for (String entry : entries) {
            String normalized = normalize(entry);
            if (normalized.isEmpty()) {
                continue;
            }
            if (!packageName.isEmpty() && !normalized.startsWith(packageName + ".")) {
                out.add(packageName + "." + normalized);
            } else {
                out.add(normalized);
            }
        }
    }

    private static List<String> immutableCopy(List<String> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> copy = new ArrayList<>();
        for (String value : source) {
            String normalized = normalize(value);
            if (!normalized.isEmpty()) {
                copy.add(normalized);
            }
        }
        return Collections.unmodifiableList(copy);
    }

    private static String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        return value.trim();
    }
}
