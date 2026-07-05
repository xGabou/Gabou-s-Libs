package net.gabou.gaboulibs.devtools.mixinvalidator;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ValidationReport {
    private final List<ValidationIssue> issues = new ArrayList<>();

    public void add(ValidationIssue issue) {
        issues.add(issue);
    }

    public void error(String location, String message, String suggestion) {
        add(ValidationIssue.error(location, message, suggestion));
    }

    public void warning(String location, String message, String suggestion) {
        add(ValidationIssue.warning(location, message, suggestion));
    }

    public void info(String location, String message, String suggestion) {
        add(ValidationIssue.info(location, message, suggestion));
    }

    public List<ValidationIssue> getIssues() {
        return Collections.unmodifiableList(issues);
    }

    public boolean hasErrors() {
        return issues.stream().anyMatch(issue -> issue.getSeverity() == ValidationSeverity.ERROR);
    }

    public boolean hasWarnings() {
        return issues.stream().anyMatch(issue -> issue.getSeverity() == ValidationSeverity.WARNING);
    }

    public int count(ValidationSeverity severity) {
        int total = 0;
        for (ValidationIssue issue : issues) {
            if (issue.getSeverity() == severity) {
                total++;
            }
        }
        return total;
    }

    public void print(PrintStream out) {
        out.println();
        out.println("GabouLibs Mixin Validator");
        out.println("==========================");

        if (issues.isEmpty()) {
            out.println("OK: no mixin validation issues found.");
            out.println();
            return;
        }

        for (ValidationIssue issue : issues) {
            out.println("[" + issue.getSeverity() + "] " + issue.getMessage());
            if (!issue.getLocation().isEmpty()) {
                out.println("  at: " + issue.getLocation());
            }
            if (!issue.getSuggestion().isEmpty()) {
                out.println("  fix: " + issue.getSuggestion());
            }
            out.println();
        }

        Map<ValidationSeverity, Integer> counts = new EnumMap<>(ValidationSeverity.class);
        for (ValidationSeverity severity : ValidationSeverity.values()) {
            counts.put(severity, count(severity));
        }
        out.println("Summary: "
                + counts.get(ValidationSeverity.ERROR) + " error(s), "
                + counts.get(ValidationSeverity.WARNING) + " warning(s), "
                + counts.get(ValidationSeverity.INFO) + " info item(s).");
        out.println();
    }
}
