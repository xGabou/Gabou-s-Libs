package net.Gabou.gaboulibs.auth;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PendingAuthManager {

    private static final int AUTH_CHALLENGE_TIMEOUT_TICKS = 20 * 60;

    public record PendingAuth(
            long nonce,
            long issuedAtMs,
            long sendAtTick,
            boolean sent,
            int attempts,
            boolean offlineUuidVersionThree
    ) {
    }

    private static final Map<UUID, PendingAuth> PENDING = new HashMap<>();

    private PendingAuthManager() {
    }

    /**
     * Starts a pending authentication challenge for a player.
     *
     * @param player the player being authenticated
     * @param currentTick the current server tick
     * @return the created pending authentication state
     */
    public static PendingAuth begin(ServerPlayer player, long currentTick) {
        PendingAuth pending = new PendingAuth(
                SharedSecret.createNonce(),
                System.currentTimeMillis(),
                currentTick + 40L,
                false,
                0,
                player != null && player.getUUID() != null && player.getUUID().version() == 3
        );

        if (player != null) {
            PENDING.put(player.getUUID(), pending);
        }

        return pending;
    }

    /**
     * Marks a pending authentication challenge as sent.
     *
     * @param uuid the player UUID
     */
    public static void markSent(UUID uuid) {
        PendingAuth pending = get(uuid);
        if (pending == null) {
            return;
        }

        PENDING.put(uuid, new PendingAuth(
                pending.nonce(),
                pending.issuedAtMs(),
                pending.sendAtTick(),
                true,
                pending.attempts() + 1,
                pending.offlineUuidVersionThree()
        ));
    }

    /**
     * Schedules another authentication challenge attempt.
     *
     * @param uuid the player UUID
     * @param currentTick the current server tick
     */
    public static void scheduleRetry(UUID uuid, long currentTick) {
        PendingAuth pending = get(uuid);
        if (pending == null) {
            return;
        }

        PENDING.put(uuid, new PendingAuth(
                pending.nonce(),
                pending.issuedAtMs(),
                currentTick + 100L,
                false,
                pending.attempts(),
                pending.offlineUuidVersionThree()
        ));
    }

    public static PendingAuth get(UUID uuid) {
        return uuid == null ? null : PENDING.get(uuid);
    }

    public static boolean isPending(UUID uuid) {
        return uuid != null && PENDING.containsKey(uuid);
    }

    public static void clear(UUID uuid) {
        if (uuid != null) {
            PENDING.remove(uuid);
        }
    }

    public static void clear(ServerPlayer player) {
        if (player != null) {
            clear(player.getUUID());
        }
    }

    public static Map<UUID, PendingAuth> snapshot() {
        return new HashMap<>(PENDING);
    }

    public static long getTimeoutMs() {
        return AUTH_CHALLENGE_TIMEOUT_TICKS * 50L;
    }
}
