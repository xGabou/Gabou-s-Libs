package net.Gabou.gaboulibs.auth;

import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import java.security.Permission;
import java.util.concurrent.ThreadLocalRandom;

public final class AuthGuards {
    private AuthGuards() {
    }

    private final static double authAbilityFailureChance = 0.35D;
    private final static float authCooldownMultiplier = 2.0f;

    public static boolean isLikelyOfflineUuid(ServerPlayer player) {
        return player != null && player.getUUID() != null && player.getUUID().version() == 3;
    }

    public static boolean canUseProtectedFeature(ServerPlayer player) {
        if (player == null) {
            return false;
        }
        if (player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
            return true;
        }
        return !PendingAuthManager.isPending(player.getUUID()) && !SuspiciousPlayers.isSuspicious(player.getUUID());
    }

    public static boolean shouldSabotageFeatureUse(ServerPlayer player) {
        if (player == null || canUseProtectedFeature(player)) {
            return false;
        }
        double chance = Math.max(0.0D, Math.min(1.0D, authAbilityFailureChance));
        return ThreadLocalRandom.current().nextDouble() < chance;
    }

    public static int inflateCooldown(int baseCooldown) {
        if (baseCooldown <= 0) {
            return 20;
        }
        float multiplier = Math.max(1.0F, authCooldownMultiplier);
        return Math.max(baseCooldown + 1, Math.round(baseCooldown * multiplier));
    }
}
