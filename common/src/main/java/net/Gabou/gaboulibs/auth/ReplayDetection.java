package net.Gabou.gaboulibs.auth;

public record ReplayDetection(ReplayEnvironment environment, String reason) {
    public static ReplayDetection none(String reason) {
        return new ReplayDetection(ReplayEnvironment.NONE, reason == null ? "" : reason);
    }

    public static ReplayDetection detected(ReplayEnvironment environment, String reason) {
        return new ReplayDetection(environment, reason == null ? "" : reason);
    }

    public boolean detected() {
        return environment != ReplayEnvironment.NONE;
    }
}
