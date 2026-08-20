package net.Gabou.gaboulibs.auth;

import dev.architectury.platform.Platform;
import net.Gabou.gaboulibs.Gaboulibs;
import net.Gabou.gaboulibs.ModNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;

public final class ServerAuth {
    private static final long ACCOUNT_VERIFICATION_TIMEOUT_MS = 60_000L;

    private ServerAuth() {
    }
    private final static boolean authStrictOfflineUuidReject = false;

    public static boolean onLogin(ServerPlayer player) {
        onLogout(player);

        if (Platform.isDevelopmentEnvironment()) {
            Gaboulibs.LOGGER.info(
                    "Skipping launcher/auth checks for {} because the game is running in a development environment.",
                    player.getGameProfile().name()
            );
            return true;
        }

        AuthResult joinResult = verifyJoin(player, player.getGameProfile());
        if (!joinResult.allowed()) {
            Gaboulibs.LOGGER.warn(
                    "Rejected {} during launcher verification: {}",
                    player.getGameProfile().name(),
                    joinResult.reason()
            );
            player.connection.disconnect(Component.literal("Authentication rejected."));
            return false;
        }

        if (AuthGuards.isLikelyOfflineUuid(player) && authStrictOfflineUuidReject) {
            Gaboulibs.LOGGER.warn(
                "Rejected {} because strict auth mode disallows offline UUID v3 identities.",
                player.getGameProfile().name()
            );
            player.connection.disconnect(Component.literal("Authentication rejected."));
            return false;
        }

        PendingAuthManager.begin(player, player.level().getServer().getTickCount());
        return true;
    }

    public static AuthResult verifyJoin(ServerPlayer player, com.mojang.authlib.GameProfile profile) {
        String launcherReason = ClientLauncherGuards.getDetectedReason();
        if (launcherReason != null && !launcherReason.isBlank()) {
            return AuthResult.block("TLauncher or invalid launcher detected: " + launcherReason);
        }

        McAccountResult accountResult = MinecraftAccountVerifier.verifyWithTimeout(
                profile,
                ACCOUNT_VERIFICATION_TIMEOUT_MS
        );
        if (accountResult.passed()) {
            return AuthResult.allow(AuthTrustLevel.VERIFIED);
        }

        Gaboulibs.LOGGER.warn(
                "Minecraft account verification failed or timed out for {}: {}. Allowing because launcher detection passed.",
                profile == null ? "<unknown>" : profile.name(),
                accountResult.reason()
        );
        return AuthResult.allow(AuthTrustLevel.TLAUNCHER_CHECK_PASSED);
    }

    public static void sendChallenge(ServerPlayer player) {
        if (player == null) {
            return;
        }

        PendingAuthManager.PendingAuth pending = PendingAuthManager.get(player.getUUID());
        if (pending == null) {
            return;
        }

        ModNetworking.sendToPlayer(player, new S2CChallengePacket(pending.nonce()));
    }

    public static void onTick(MinecraftServer server) {
        if (server == null) {
            return;
        }

        long now = System.currentTimeMillis();
        long timeoutMs = PendingAuthManager.getTimeoutMs();
        long currentTick = server.getTickCount();

        for (Map.Entry<UUID, PendingAuthManager.PendingAuth> entry : PendingAuthManager.snapshot().entrySet()) {
            UUID uuid = entry.getKey();
            PendingAuthManager.PendingAuth pending = entry.getValue();

            if (pending == null) {
                continue;
            }

            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            if (player == null) {
                PendingAuthManager.clear(uuid);
                continue;
            }

            if (!pending.sent() && currentTick >= pending.sendAtTick()) {
                ServerAuth.sendChallenge(player);
                PendingAuthManager.markSent(uuid);
                continue;
            }

            if (pending.sent() && pending.attempts() < 3 && now - pending.issuedAtMs() > 5000L * pending.attempts()) {
                PendingAuthManager.scheduleRetry(uuid, currentTick);
                continue;
            }

            if (now - pending.issuedAtMs() < timeoutMs) {
                continue;
            }

            PendingAuthManager.clear(uuid);

            Gaboulibs.LOGGER.warn(
                    "Auth challenge timed out for {}; allowing because launcher detection already passed.",
                    player.getGameProfile().name()
            );
        }
    }

    public static void onLogout(ServerPlayer player) {
        if (player == null) {
            return;
        }

        PendingAuthManager.clear(player);
        SuspiciousPlayers.clear(player);
    }

    public static void handleChallengeReply(ServerPlayer player, C2SChallengeReplyPacket packet) {
        if (player == null || packet == null) {
            return;
        }

        String launcherReason = packet.launcherReason();
        if (launcherReason != null && !launcherReason.isBlank() && player.level() instanceof ServerLevel serverLevel) {
            TLauncherDetectedHandler.handle(serverLevel, player, launcherReason);
            PendingAuthManager.clear(player);
            SuspiciousPlayers.clear(player);
            return;
        }

        PendingAuthManager.PendingAuth pending = PendingAuthManager.get(player.getUUID());
        if (pending == null) {
            markInvalid(player, "unexpected auth reply");
            return;
        }
        if (pending.nonce() != packet.nonce()) {
            markInvalid(player, "nonce mismatch");
            return;
        }
        if (!SharedSecret.verifyResponse(player.getUUID(), packet.nonce(), packet.response())) {
            markInvalid(player, "invalid auth response");
            return;
        }

        PendingAuthManager.clear(player);
        SuspiciousPlayers.clear(player);
        Gaboulibs.LOGGER.info("Auth challenge completed for {}", player.getGameProfile().name());
    }

    private static void markInvalid(ServerPlayer player, String reason) {
        PendingAuthManager.clear(player);
        Gaboulibs.LOGGER.warn(
            "Auth verification failed for {}; allowing because launcher detection already passed: {}",
            player.getGameProfile().name(),
            reason
        );
    }
}
