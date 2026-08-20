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
    public static boolean onLogin(ServerPlayer player) {
        onLogout(player);

        ReplayDetection replayDetection = ReplayCompatibility.detectServerReplay(player);
        if (replayDetection.detected()) {
            Gaboulibs.LOGGER.info(
                    "Replay identity detected for {}; using {} compatibility path: {}.",
                    profileName(player), replayDetection.environment(), replayDetection.reason()
            );
            return true;
        }

        if (Platform.isDevelopmentEnvironment()) {
            Gaboulibs.LOGGER.info(
                    "Skipping launcher/auth checks for {} because the game is running in a development environment.",
                    player.getGameProfile().name()
            );
            return true;
        }

        AuthResult joinResult = verifyJoin(player, player.getGameProfile());
        if (!joinResult.allowed()) {
            reject(player, joinResult.reason());
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

        boolean serverUsesAuthentication = player != null
                && player.level().getServer() != null
                && player.level().getServer().usesAuthentication();
        McAccountResult accountResult = MinecraftAccountVerifier.verifyWithTimeout(
                profile,
                serverUsesAuthentication,
                ACCOUNT_VERIFICATION_TIMEOUT_MS
        );
        if (accountResult.passed()) {
            return AuthResult.allow(AuthTrustLevel.VERIFIED);
        }

        return AuthResult.block("Minecraft account verification failed: " + accountResult.reason());
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
            reject(player, "authentication challenge timed out");
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
        reject(player, "authentication challenge failed: " + reason);
    }

    private static void reject(ServerPlayer player, String reason) {
        Gaboulibs.LOGGER.warn("Rejected {} during authentication: {}.", profileName(player), reason);
        player.connection.disconnect(Component.literal("Authentication rejected."));
    }

    private static String profileName(ServerPlayer player) {
        if (player == null || player.getGameProfile() == null || player.getGameProfile().name() == null) return "<unknown>";
        return player.getGameProfile().name();
    }
}
