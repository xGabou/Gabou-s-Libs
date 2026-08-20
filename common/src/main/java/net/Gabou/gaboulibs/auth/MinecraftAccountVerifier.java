package net.Gabou.gaboulibs.auth;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class MinecraftAccountVerifier {
    private static final ExecutorService AUTH_EXECUTOR = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable, "identity2-account-verifier");
        thread.setDaemon(true);
        return thread;
    });

    private MinecraftAccountVerifier() {
    }

    public static McAccountResult verify(GameProfile profile) {
        return verifyProfile(profile == null ? null : profile.id(), profile == null ? null : profile.name(), true);
    }

    public static McAccountResult verify(GameProfile profile, boolean serverUsesAuthentication) {
        return verifyProfile(profile == null ? null : profile.id(), profile == null ? null : profile.name(), serverUsesAuthentication);
    }

    static McAccountResult verifyProfile(UUID uuid, String profileName, boolean serverUsesAuthentication) {
        if (uuid == null || profileName == null || profileName.isBlank()) return McAccountResult.failed("missing game profile identity");
        if (!serverUsesAuthentication) return McAccountResult.failed("server is not using authenticated online mode");
        if (uuid.version() == 3) return McAccountResult.failed("offline UUID version 3 profile");
        if (uuid.version() != 4) return McAccountResult.failed("unsupported UUID version " + uuid.version());
        return McAccountResult.success();
    }

    public static McAccountResult verifyWithTimeout(GameProfile profile, boolean serverUsesAuthentication, long timeoutMillis) {
        try {
            return CompletableFuture
                    .supplyAsync(() -> verify(profile, serverUsesAuthentication), AUTH_EXECUTOR)
                    .get(Math.max(1L, timeoutMillis), TimeUnit.MILLISECONDS);
        } catch (Exception exception) {
            return McAccountResult.failed("verification timed out or failed");
        }
    }
}
