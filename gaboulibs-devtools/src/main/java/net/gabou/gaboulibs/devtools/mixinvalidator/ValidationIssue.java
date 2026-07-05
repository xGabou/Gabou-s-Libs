package net.gabou.gaboulibs.devtools.mixinvalidator;

import java.util.Objects;

public final class ValidationIssue {
    private final ValidationSeverity severity;
    private final String location;
    private final String message;
    private final String suggestion;

    public ValidationIssue(ValidationSeverity severity, String location, String message, String suggestion) {
        this.severity = Objects.requireNonNull(severity, "severity");
        this.location = normalize(location);
        this.message = Objects.requireNonNull(message, "message");
        this.suggestion = normalize(suggestion);
    }

    public static ValidationIssue error(String location, String message, String suggestion) {
        return new ValidationIssue(ValidationSeverity.ERROR, location, message, suggestion);
    }

    public static ValidationIssue warning(String location, String message, String suggestion) {
        return new ValidationIssue(ValidationSeverity.WARNING, location, message, suggestion);
    }

    public static ValidationIssue info(String location, String message, String suggestion) {
        return new ValidationIssue(ValidationSeverity.INFO, location, message, suggestion);
    }

    public ValidationSeverity getSeverity() {
        return severity;
    }

    public String getLocation() {
        return location;
    }

    public String getMessage() {
        return message;
    }

    public String getSuggestion() {
        return suggestion;
    }

    private static String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        return value.trim();
    }
}
