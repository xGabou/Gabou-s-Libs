package net.Gabou.gaboulibs.auth;

import net.Gabou.gaboulibs.Gaboulibs;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.*;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

public final class TLauncherDetectedHandler {
    private static final String BAN_SOURCE = "Gaboulibs";

    private TLauncherDetectedHandler() {
    }

    public static void handle(ServerLevel level, ServerPlayer player, String reason) {
        if (level == null || player == null || reason == null || reason.isBlank()) {
            return;
        }

        handle(level, player.getUUID(), player.getGameProfile().name(), reason);
        banIp(level, player, reason);
        disconnect(player, reason);
    }

    public static void handle(ServerLevel level, UUID uuid, String playerName, String reason) {
        if (level == null || uuid == null || reason == null || reason.isBlank()) {
            return;
        }

        NameAndId nameAndId = new NameAndId(uuid, playerName);
        UserBanList bans = level.getServer().getPlayerList().getBans();
        if (!bans.isBanned(nameAndId)) {
            bans.add(new UserBanListEntry(nameAndId, new Date(), BAN_SOURCE, null, reason));
            saveBanLists(level);
            Gaboulibs.LOGGER.error(
                "Banned launcher-violating player {} ({}) on {}: {}",
                playerName,
                uuid,
                level.dimension().identifier(),
                reason
            );
        }
    }

    private static void banIp(ServerLevel level, ServerPlayer player, String reason) {
        String ipAddress = player.getIpAddress();
        if (ipAddress == null || ipAddress.isBlank() || "<unknown>".equals(ipAddress)) {
            return;
        }

        IpBanList ipBans = level.getServer().getPlayerList().getIpBans();
        if (ipBans.isBanned(ipAddress)) {
            return;
        }

        ipBans.add(new IpBanListEntry(ipAddress, new Date(), BAN_SOURCE, null, reason));
        saveBanLists(level);
        Gaboulibs.LOGGER.error("Banned launcher-violating IP {} on {}: {}", ipAddress, level.dimension().identifier(), reason);
    }

    private static void saveBanLists(ServerLevel level) {
        try {
            level.getServer().getPlayerList().getBans().save();
            level.getServer().getPlayerList().getIpBans().save();
        } catch (IOException e) {
            Gaboulibs.LOGGER.warn("Failed to persist launcher violation ban lists", e);
        }
    }

    private static void disconnect(ServerPlayer player, String reason) {
        if (player.connection != null) {
            player.connection.disconnect(Component.literal("Launcher violation detected: " + reason));
        }
    }
}
