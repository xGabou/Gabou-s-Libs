package net.Gabou.gaboulibs.util;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;

public final class NetworkCompat {
    private NetworkCompat() {
    }

    public static <T extends NetworkPayload> void sendToPlayer(ServerPlayer player, T payload) {
        if (player == null || payload == null) {
            return;
        }
        NetworkManager.sendToPlayer(player, payload);
    }

    public static <T extends NetworkPayload> void sendToServer(T payload) {
        if (payload == null) {
            return;
        }
        NetworkManager.sendToServer(payload);
    }

    public static <T extends NetworkPayload> void registerReceiver(
        NetworkManager.Side side,
        CustomPacketPayload.Type<T> type,
        StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
        BiConsumer<T, NetworkManager.PacketContext> handler
    ) {
        NetworkManager.registerReceiver(side, type, codec, handler::accept);
    }
}


