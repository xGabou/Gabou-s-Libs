package net.Gabou.gaboulibs.auth;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.architectury.platform.Platform;
import net.Gabou.gaboulibs.Gaboulibs;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.UUID;

public final class ReplayCompatibility {
    static final String FLASHBACK_MOD_ID = "flashback";
    static final String REPLAYMOD_MOD_ID = "replaymod";
    static final String FLASHBACK_SERVER_CLASS = "com.moulberry.flashback.playback.ReplayServer";
    static final String FLASHBACK_PLAYER_CLASS = "com.moulberry.flashback.playback.ReplayPlayer";
    static final String FLASHBACK_VIEWER_NAME = "Replay Viewer";
    static final String FLASHBACK_VIEWER_PROPERTY = "IsReplayViewer";

    private ReplayCompatibility() {
    }

    public static void logModStatus() {
        Gaboulibs.LOGGER.info(
                "Replay compatibility status: Flashback detected={}, ReplayMod detected={}.",
                Platform.isModLoaded(FLASHBACK_MOD_ID),
                Platform.isModLoaded(REPLAYMOD_MOD_ID)
        );
    }

    public static ReplayDetection detectServerReplay(ServerPlayer player) {
        if (player == null) return ReplayDetection.none("missing server player");
        GameProfile profile = player.getGameProfile();
        UUID uuid = profile == null ? null : profile.id();
        String name = profile == null ? null : profile.name();
        ReplayDetection detection = classifyFlashback(
                Platform.isModLoaded(FLASHBACK_MOD_ID),
                hierarchyContains(player.level().getServer(), FLASHBACK_SERVER_CLASS),
                hierarchyContains(player, FLASHBACK_PLAYER_CLASS),
                uuid,
                name,
                hasFlashbackViewerMarker(profile)
        );
        if (!detection.detected() && uuid != null && uuid.version() == 3 && Platform.isModLoaded(FLASHBACK_MOD_ID)) {
            Gaboulibs.LOGGER.debug("Version-3 profile was not accepted as Flashback replay identity: {}.", detection.reason());
        }
        return detection;
    }

    static ReplayDetection classifyFlashback(
            boolean modLoaded,
            boolean replayServer,
            boolean replayPlayer,
            UUID uuid,
            String profileName,
            boolean viewerMarker
    ) {
        if (!modLoaded) return ReplayDetection.none("Flashback is not loaded");
        if (!replayServer) return ReplayDetection.none("server is not Flashback ReplayServer");
        if (!replayPlayer) return ReplayDetection.none("player is not Flashback ReplayPlayer");
        if (!FLASHBACK_VIEWER_NAME.equals(profileName)) return ReplayDetection.none("profile name is not Flashback replay viewer name");
        if (!viewerMarker) return ReplayDetection.none("profile is missing Flashback replay viewer marker");
        if (uuid == null || uuid.version() != 3) return ReplayDetection.none("profile does not use Flashback replay UUID format");
        return ReplayDetection.detected(
                ReplayEnvironment.FLASHBACK,
                "matched Flashback ReplayServer, ReplayPlayer, and replay viewer profile marker"
        );
    }

    private static boolean hasFlashbackViewerMarker(GameProfile profile) {
        if (profile == null || profile.properties() == null) return false;
        Collection<Property> properties = profile.properties().get(FLASHBACK_VIEWER_PROPERTY);
        return properties != null && properties.stream()
                .anyMatch(property -> property != null && "true".equalsIgnoreCase(property.value()));
    }

    private static boolean hierarchyContains(Object value, String expectedClassName) {
        if (value == null) return false;
        Class<?> type = value.getClass();
        while (type != null) {
            if (expectedClassName.equals(type.getName())) return true;
            type = type.getSuperclass();
        }
        return false;
    }
}
