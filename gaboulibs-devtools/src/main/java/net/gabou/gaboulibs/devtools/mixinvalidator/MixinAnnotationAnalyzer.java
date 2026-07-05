package net.gabou.gaboulibs.devtools.mixinvalidator;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

public final class MixinAnnotationAnalyzer {
    private static final String INJECT = "Lorg/spongepowered/asm/mixin/injection/Inject;";
    private static final String REDIRECT = "Lorg/spongepowered/asm/mixin/injection/Redirect;";
    private static final String MODIFY_ARG = "Lorg/spongepowered/asm/mixin/injection/ModifyArg;";
    private static final String MODIFY_ARGS = "Lorg/spongepowered/asm/mixin/injection/ModifyArgs;";
    private static final String MODIFY_VARIABLE = "Lorg/spongepowered/asm/mixin/injection/ModifyVariable;";
    private static final String MODIFY_CONSTANT = "Lorg/spongepowered/asm/mixin/injection/ModifyConstant;";
    private static final String ACCESSOR = "Lorg/spongepowered/asm/mixin/gen/Accessor;";
    private static final String INVOKER = "Lorg/spongepowered/asm/mixin/gen/Invoker;";
    private static final String AT = "Lorg/spongepowered/asm/mixin/injection/At;";

    public void analyzeMethodAnnotations(String mixinClassName, MethodNode method, ValidationReport report) {
        for (AnnotationNode annotation : allAnnotations(method)) {
            if (isInjectionAnnotation(annotation.desc)) {
                analyzeInjectionAnnotation(mixinClassName, method, annotation, report);
            } else if (ACCESSOR.equals(annotation.desc)) {
                analyzeAccessor(mixinClassName, method, annotation, report);
            } else if (INVOKER.equals(annotation.desc)) {
                analyzeInvoker(mixinClassName, method, annotation, report);
            }
        }
    }

    public static Object annotationValue(AnnotationNode annotation, String name) {
        if (annotation.values == null) {
            return null;
        }
        for (int index = 0; index < annotation.values.size() - 1; index += 2) {
            if (name.equals(annotation.values.get(index))) {
                return annotation.values.get(index + 1);
            }
        }
        return null;
    }

    public static List<AnnotationNode> allClassAnnotations(org.objectweb.asm.tree.ClassNode node) {
        List<AnnotationNode> annotations = new ArrayList<>();
        if (node.visibleAnnotations != null) {
            annotations.addAll(node.visibleAnnotations);
        }
        if (node.invisibleAnnotations != null) {
            annotations.addAll(node.invisibleAnnotations);
        }
        return annotations;
    }

    private void analyzeInjectionAnnotation(String mixinClassName, MethodNode method, AnnotationNode annotation, ValidationReport report) {
        String location = location(mixinClassName, method);
        String annotationName = simpleName(annotation.desc);

        if (isMissing(annotationValue(annotation, "method"))) {
            report.warning(
                    location,
                    "@" + annotationName + " does not declare a method target.",
                    "Add method = \"...\" unless another processor intentionally supplies it."
            );
        }

        if (MODIFY_CONSTANT.equals(annotation.desc)) {
            if (isMissing(annotationValue(annotation, "constant"))) {
                report.warning(
                        location,
                        "@ModifyConstant does not declare a constant selector.",
                        "Add constant = @Constant(...) so the validator can see the intended constant."
                );
            }
            return;
        }

        Object atValue = annotationValue(annotation, "at");
        if (isMissing(atValue)) {
            report.warning(
                    location,
                    "@" + annotationName + " does not declare an @At injection point.",
                    "Add at = @At(\"...\") to make the injection point explicit."
            );
            return;
        }

        for (AnnotationNode at : annotationNodes(atValue)) {
            if (!AT.equals(at.desc)) {
                continue;
            }
            if (isMissing(annotationValue(at, "value"))) {
                report.warning(
                        location,
                        "@" + annotationName + " contains an @At without a value.",
                        "Set @At(\"HEAD\"), @At(\"RETURN\"), @At(value = \"INVOKE\", target = \"...\"), or another explicit value."
                );
            }
        }
    }

    private void analyzeAccessor(String mixinClassName, MethodNode method, AnnotationNode annotation, ValidationReport report) {
        if (!isMissing(annotationValue(annotation, "value"))) {
            return;
        }
        if (method.name.startsWith("get") || method.name.startsWith("set") || method.name.startsWith("is")) {
            return;
        }
        report.warning(
                location(mixinClassName, method),
                "@Accessor relies on a method name that does not look like a conventional accessor.",
                "Set @Accessor(\"fieldName\") if this name is intentional."
        );
    }

    private void analyzeInvoker(String mixinClassName, MethodNode method, AnnotationNode annotation, ValidationReport report) {
        if (!isMissing(annotationValue(annotation, "value"))) {
            return;
        }
        if (method.name.startsWith("call") || method.name.startsWith("invoke") || method.name.startsWith("new")) {
            return;
        }
        report.warning(
                location(mixinClassName, method),
                "@Invoker relies on a method name that does not look like a conventional invoker.",
                "Set @Invoker(\"methodName\") if this name is intentional."
        );
    }

    private static List<AnnotationNode> allAnnotations(MethodNode method) {
        List<AnnotationNode> annotations = new ArrayList<>();
        if (method.visibleAnnotations != null) {
            annotations.addAll(method.visibleAnnotations);
        }
        if (method.invisibleAnnotations != null) {
            annotations.addAll(method.invisibleAnnotations);
        }
        return annotations;
    }

    private static boolean isInjectionAnnotation(String descriptor) {
        return INJECT.equals(descriptor)
                || REDIRECT.equals(descriptor)
                || MODIFY_ARG.equals(descriptor)
                || MODIFY_ARGS.equals(descriptor)
                || MODIFY_VARIABLE.equals(descriptor)
                || MODIFY_CONSTANT.equals(descriptor);
    }

    private static List<AnnotationNode> annotationNodes(Object value) {
        List<AnnotationNode> nodes = new ArrayList<>();
        if (value instanceof AnnotationNode) {
            nodes.add((AnnotationNode) value);
        } else if (value instanceof List<?>) {
            for (Object item : (List<?>) value) {
                if (item instanceof AnnotationNode) {
                    nodes.add((AnnotationNode) item);
                }
            }
        }
        return nodes;
    }

    private static boolean isMissing(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String) {
            return ((String) value).trim().isEmpty();
        }
        if (value instanceof List<?>) {
            return ((List<?>) value).isEmpty();
        }
        return false;
    }

    private static String location(String mixinClassName, MethodNode method) {
        return mixinClassName + "#" + method.name + method.desc;
    }

    private static String simpleName(String descriptor) {
        int end = descriptor.lastIndexOf(';');
        int slash = descriptor.lastIndexOf('/');
        if (slash >= 0 && end > slash) {
            return descriptor.substring(slash + 1, end);
        }
        return descriptor;
    }
}
