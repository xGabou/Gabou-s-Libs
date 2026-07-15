package net.Gabou.gaboulibs;

import net.Gabou.gaboulibs.auth.C2SChallengeReplyPacket;
import net.Gabou.gaboulibs.auth.S2CChallengePacket;
import net.Gabou.gaboulibs.platform.ModNetworkingPlatform;
import net.Gabou.gaboulibs.util.NetworkPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public final class ModNetworking {
    public static final Identifier AUTH_CHALLENGE_PACKET_ID = Identifier.fromNamespaceAndPath(Gaboulibs.MOD_ID, "auth_challenge");
    public static final Identifier AUTH_CHALLENGE_REPLY_PACKET_ID = Identifier.fromNamespaceAndPath(Gaboulibs.MOD_ID, "auth_challenge_reply");

    private static ModNetworkingPlatform platform = ModNetworkingPlatform.NOOP;
    private static boolean initialized = false;
    private static boolean clientInitialized = false;

    private ModNetworking() {
    }

    public static void setPlatform(ModNetworkingPlatform networkingPlatform) {
        platform = networkingPlatform == null ? ModNetworkingPlatform.NOOP : networkingPlatform;
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        platform.registerCommonPackets();
    }

    public static void initClient() {
        if (clientInitialized) {
            return;
        }
        clientInitialized = true;
        platform.registerClientPackets();
    }

    public static void sendToPlayer(ServerPlayer player, NetworkPayload payload) {
        if (player == null || payload == null) {
            return;
        }
        platform.sendToPlayer(player, payload);
    }

    public static void sendToServer(NetworkPayload payload) {
        if (payload == null) {
            return;
        }
        platform.sendToServer(payload);
    }

    public static boolean isAuthPacket(NetworkPayload payload) {
        return payload instanceof S2CChallengePacket || payload instanceof C2SChallengeReplyPacket;
    }
}
