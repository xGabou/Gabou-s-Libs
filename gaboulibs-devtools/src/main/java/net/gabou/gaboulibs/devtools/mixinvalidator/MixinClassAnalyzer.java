package net.gabou.gaboulibs.devtools.mixinvalidator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MixinClassAnalyzer {
    private static final String MIXIN = "Lorg/spongepowered/asm/mixin/Mixin;";
    private static final String PSEUDO = "Lorg/spongepowered/asm/mixin/Pseudo;";

    private final MixinAnnotationAnalyzer annotationAnalyzer = new MixinAnnotationAnalyzer();

    public AnalysisResult analyze(String declaredClassName, byte[] classBytes, ValidationReport report) {
        ClassNode classNode = new ClassNode();
        try {
            new ClassReader(classBytes).accept(classNode, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        } catch (RuntimeException ex) {
            report.error(
                    declaredClassName,
                    "Could not parse mixin class bytecode: " + ex.getMessage(),
                    "Recompile the project and check for corrupt class output."
            );
            return AnalysisResult.empty(declaredClassName);
        }

        String actualClassName = classNode.name.replace('/', '.');
        if (!declaredClassName.equals(actualClassName)) {
            report.warning(
                    declaredClassName,
                    "Mixin config entry resolved to class " + actualClassName + ".",
                    "Check the package in the mixin JSON if this is not expected."
            );
        }

        AnnotationNode mixinAnnotation = null;
        boolean pseudo = false;
        for (AnnotationNode annotation : MixinAnnotationAnalyzer.allClassAnnotations(classNode)) {
            if (MIXIN.equals(annotation.desc)) {
                mixinAnnotation = annotation;
            } else if (PSEUDO.equals(annotation.desc)) {
                pseudo = true;
            }
        }

        if (mixinAnnotation == null) {
            report.error(
                    actualClassName,
                    "Class is declared in a mixin config but is missing @Mixin.",
                    "Add @Mixin(...) or remove this class from the mixin JSON."
            );
            return new AnalysisResult(actualClassName, Collections.emptyList(), false, pseudo);
        }

        List<String> targets = readMixinTargets(mixinAnnotation);
        if (targets.isEmpty()) {
            report.warning(
                    actualClassName,
                    "@Mixin does not declare value or targets.",
                    "Declare the target class using @Mixin(TargetClass.class) or @Mixin(targets = \"...\")."
            );
        }

        for (MethodNode method : classNode.methods) {
            annotationAnalyzer.analyzeMethodAnnotations(actualClassName, method, report);
        }

        return new AnalysisResult(actualClassName, targets, true, pseudo);
    }

    private static List<String> readMixinTargets(AnnotationNode mixinAnnotation) {
        List<String> targets = new ArrayList<>();
        addTypeTargets(targets, MixinAnnotationAnalyzer.annotationValue(mixinAnnotation, "value"));
        addStringTargets(targets, MixinAnnotationAnalyzer.annotationValue(mixinAnnotation, "targets"));
        return Collections.unmodifiableList(targets);
    }

    private static void addTypeTargets(List<String> targets, Object value) {
        if (value instanceof Type) {
            addTarget(targets, ((Type) value).getClassName());
        } else if (value instanceof List<?>) {
            for (Object item : (List<?>) value) {
                if (item instanceof Type) {
                    addTarget(targets, ((Type) item).getClassName());
                }
            }
        }
    }

    private static void addStringTargets(List<String> targets, Object value) {
        if (value instanceof String) {
            addTarget(targets, (String) value);
        } else if (value instanceof List<?>) {
            for (Object item : (List<?>) value) {
                if (item instanceof String) {
                    addTarget(targets, (String) item);
                }
            }
        }
    }

    private static void addTarget(List<String> targets, String target) {
        String normalized = TargetResolver.normalizeClassName(target);
        if (!normalized.isEmpty() && !targets.contains(normalized)) {
            targets.add(normalized);
        }
    }

    public static final class AnalysisResult {
        private final String mixinClassName;
        private final List<String> targetClassNames;
        private final boolean hasMixinAnnotation;
        private final boolean hasPseudoAnnotation;

        private AnalysisResult(String mixinClassName, List<String> targetClassNames, boolean hasMixinAnnotation, boolean hasPseudoAnnotation) {
            this.mixinClassName = mixinClassName;
            this.targetClassNames = Collections.unmodifiableList(new ArrayList<>(targetClassNames));
            this.hasMixinAnnotation = hasMixinAnnotation;
            this.hasPseudoAnnotation = hasPseudoAnnotation;
        }

        private static AnalysisResult empty(String mixinClassName) {
            return new AnalysisResult(mixinClassName, Collections.emptyList(), false, false);
        }

        public String getMixinClassName() {
            return mixinClassName;
        }

        public List<String> getTargetClassNames() {
            return targetClassNames;
        }

        public boolean hasMixinAnnotation() {
            return hasMixinAnnotation;
        }

        public boolean hasPseudoAnnotation() {
            return hasPseudoAnnotation;
        }
    }
}
