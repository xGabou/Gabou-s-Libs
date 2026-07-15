package net.Gabou.gaboulibs.auth;

public record McAccountResult(boolean passed, String reason) {
    public static McAccountResult success() {
        return new McAccountResult(true, "");
    }

    public static McAccountResult failed(String reason) {
        return new McAccountResult(false, reason == null ? "" : reason);
    }
}
