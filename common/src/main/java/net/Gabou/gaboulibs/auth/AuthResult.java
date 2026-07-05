package net.Gabou.gaboulibs.auth;

public record AuthResult(boolean allowed, AuthTrustLevel trustLevel, String reason) {
    public static AuthResult allow(AuthTrustLevel trustLevel) {
        return new AuthResult(true, trustLevel, "");
    }

    public static AuthResult block(String reason) {
        return new AuthResult(false, null, reason == null ? "" : reason);
    }
}
