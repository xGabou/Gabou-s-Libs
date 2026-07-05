package net.gabou.gaboulibs.devtools.mixinvalidator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class TargetResolver {
    private final List<Path> classpath;
    private final Map<String, Optional<byte[]>> classBytesCache = new HashMap<>();

    public TargetResolver(Collection<Path> classpath) {
        this.classpath = new ArrayList<>();
        if (classpath != null) {
            for (Path path : classpath) {
                if (path != null) {
                    this.classpath.add(path.toAbsolutePath().normalize());
                }
            }
        }
    }

    public boolean classExists(String className) {
        return readClassBytes(className).isPresent();
    }

    public Optional<byte[]> readClassBytes(String className) {
        String normalizedName = normalizeClassName(className);
        if (normalizedName.isEmpty()) {
            return Optional.empty();
        }
        return classBytesCache.computeIfAbsent(normalizedName, this::readClassBytesUncached);
    }

    public static String normalizeClassName(String className) {
        if (className == null) {
            return "";
        }

        String normalized = className.trim();
        if (normalized.startsWith("L") && normalized.endsWith(";")) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        normalized = normalized.replace('/', '.');
        while (normalized.startsWith(".")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith(".")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private Optional<byte[]> readClassBytesUncached(String className) {
        String entryName = className.replace('.', '/') + ".class";
        for (Path path : classpath) {
            if (!Files.exists(path)) {
                continue;
            }

            try {
                if (Files.isDirectory(path)) {
                    Path classFile = path.resolve(entryName);
                    if (Files.isRegularFile(classFile)) {
                        return Optional.of(Files.readAllBytes(classFile));
                    }
                } else if (isArchive(path)) {
                    Optional<byte[]> bytes = readFromJar(path, entryName);
                    if (bytes.isPresent()) {
                        return bytes;
                    }
                }
            } catch (IOException ignored) {
                // Resolution failures are reported by the caller as missing classes.
            }
        }
        return Optional.empty();
    }

    private Optional<byte[]> readFromJar(Path jarPath, String entryName) throws IOException {
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            JarEntry entry = jarFile.getJarEntry(entryName);
            if (entry == null) {
                return Optional.empty();
            }
            try (InputStream inputStream = jarFile.getInputStream(entry)) {
                return Optional.of(inputStream.readAllBytes());
            }
        }
    }

    private static boolean isArchive(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".jar") || name.endsWith(".zip");
    }
}
