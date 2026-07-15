package net.Gabou.gaboulibs.auth;

import com.mojang.authlib.GameProfile;
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
        if (profile == null || profile.id() == null) {
            return McAccountResult.failed("missing game profile");
        }
        return profile.id().version() == 3
                ? McAccountResult.failed("offline UUID profile")
                : McAccountResult.success();
    }

    public static McAccountResult verifyWithTimeout(GameProfile profile, long timeoutMillis) {
        try {
            return CompletableFuture
                    .supplyAsync(() -> verify(profile), AUTH_EXECUTOR)
                    .get(Math.max(1L, timeoutMillis), TimeUnit.MILLISECONDS);
        } catch (Exception exception) {
            return McAccountResult.failed("verification timed out or failed");
        }
    }
}
