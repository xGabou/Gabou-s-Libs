package net.Gabou.gaboulibs.fabric.auth;

import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.Gabou.gaboulibs.auth.C2SChallengeReplyPacket;
import net.Gabou.gaboulibs.auth.ClientAuth;
import net.Gabou.gaboulibs.auth.S2CChallengePacket;
import net.Gabou.gaboulibs.auth.ServerAuth;
import net.Gabou.gaboulibs.platform.ModNetworkingPlatform;
import net.Gabou.gaboulibs.util.NetworkCompat;
import net.Gabou.gaboulibs.util.NetworkPayload;
import net.minecraft.server.level.ServerPlayer;

public final class Identity2FabricNetworkingPlatform implements ModNetworkingPlatform {
    private static boolean commonRegistered = false;
    private static boolean clientRegistered = false;

    @Override
    public void registerCommonPackets() {
        if (commonRegistered) {
            return;
        }
        commonRegistered = true;

        if (Platform.getEnvironment() == Env.SERVER) {
            NetworkManager.registerS2CPayloadType(S2CChallengePacket.TYPE, S2CChallengePacket.STREAM_CODEC);
        }
        NetworkCompat.registerReceiver(
            NetworkManager.c2s(),
            C2SChallengeReplyPacket.TYPE,
            C2SChallengeReplyPacket.STREAM_CODEC,
            (payload, context) -> context.queue(() -> {
                if (context.getPlayer() instanceof ServerPlayer player) {
                    ServerAuth.handleChallengeReply(player, payload);
                }
            })
        );
    }

    @Override
    public void registerClientPackets() {
        if (clientRegistered) {
            return;
        }
        clientRegistered = true;

        NetworkCompat.registerReceiver(
            NetworkManager.s2c(),
            S2CChallengePacket.TYPE,
            S2CChallengePacket.STREAM_CODEC,
            (payload, context) -> context.queue(() -> ClientAuth.handleChallenge(payload))
        );
    }

    @Override
    public void sendToPlayer(ServerPlayer player, NetworkPayload payload) {
        if (player == null || payload == null) {
            return;
        }
        NetworkCompat.sendToPlayer(player, payload);
    }

    @Override
    public void sendToServer(NetworkPayload payload) {
        if (payload == null) {
            return;
        }
        NetworkCompat.sendToServer(payload);
    }
}
