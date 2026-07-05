package net.gabou.gaboulibs.devtools.mixinvalidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class OptionalModTargetRules {
    private final List<Rule> rules;

    public OptionalModTargetRules(List<Rule> rules) {
        this.rules = Collections.unmodifiableList(new ArrayList<>(rules));
    }

    public static OptionalModTargetRules empty() {
        return new OptionalModTargetRules(Collections.emptyList());
    }

    public List<Rule> getRules() {
        return rules;
    }

    public Optional<Rule> findForTarget(String targetClassName) {
        String normalizedTarget = normalize(targetClassName);
        for (Rule rule : rules) {
            if (normalizedTarget.startsWith(normalize(rule.getClassPrefix()))) {
                return Optional.of(rule);
            }
        }
        return Optional.empty();
    }

    private static String normalize(String className) {
        return className
                .replace('/', '.')
                .replace('$', '.')
                .toLowerCase(Locale.ROOT);
    }

    public static final class Rule {
        private final String classPrefix;
        private final String requiredModId;

        public Rule(String classPrefix, String requiredModId) {
            this.classPrefix = classPrefix == null ? "" : classPrefix.trim();
            this.requiredModId = requiredModId == null ? "" : requiredModId.trim();
        }

        public String getClassPrefix() {
            return classPrefix;
        }

        public String getRequiredModId() {
            return requiredModId;
        }

        public boolean isValid() {
            return !classPrefix.isEmpty() && !requiredModId.isEmpty();
        }
    }
}
