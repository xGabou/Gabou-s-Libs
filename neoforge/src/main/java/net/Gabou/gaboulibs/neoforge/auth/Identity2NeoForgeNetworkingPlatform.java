package net.Gabou.gaboulibs.neoforge.auth;

import dev.architectury.networking.NetworkManager;
import net.Gabou.gaboulibs.auth.*;
import net.Gabou.gaboulibs.platform.ModNetworkingPlatform;
import net.Gabou.gaboulibs.util.NetworkCompat;
import net.Gabou.gaboulibs.util.NetworkPayload;
import net.minecraft.server.level.ServerPlayer;

public final class Identity2NeoForgeNetworkingPlatform implements ModNetworkingPlatform {
    private static boolean commonRegistered = false;
    private static boolean clientRegistered = false;

    @Override
    public void registerCommonPackets() {
        if (commonRegistered) {
            return;
        }
        commonRegistered = true;

        NetworkManager.registerS2CPayloadType(S2CChallengePacket.TYPE, S2CChallengePacket.STREAM_CODEC);
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
        NetworkCompat.sendToPlayer(player, payload);
    }

    @Override
    public void sendToServer(NetworkPayload payload) {
        NetworkCompat.sendToServer(payload);
    }
}
