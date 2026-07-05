package net.Gabou.gaboulibs.forge.auth;

import net.Gabou.gaboulibs.Gaboulibs;
import net.Gabou.gaboulibs.auth.*;
import net.Gabou.gaboulibs.platform.ModNetworkingPlatform;
import net.Gabou.gaboulibs.util.NetworkPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Function;
import java.util.function.Supplier;

public final class Identity2ForgeNetworkingPlatform implements ModNetworkingPlatform {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
        .named(new ResourceLocation(Gaboulibs.MOD_ID, "auth"))
        .networkProtocolVersion(() -> PROTOCOL_VERSION)
        .clientAcceptedVersions(PROTOCOL_VERSION::equals)
        .serverAcceptedVersions(PROTOCOL_VERSION::equals)
        .simpleChannel();

    private static boolean registered = false;

    @Override
    public void registerCommonPackets() {
        if (registered) {
            return;
        }
        registered = true;

        registerMessage(
            0,
            S2CChallengePacket.class,
            NetworkDirection.PLAY_TO_CLIENT,
            S2CChallengePacket::decode,
            (packet, contextSupplier) -> {
                NetworkEvent.Context context = contextSupplier.get();
                context.enqueueWork(() -> ClientAuth.handleChallenge(packet));
                context.setPacketHandled(true);
            }
        );

        registerMessage(
            1,
            C2SChallengeReplyPacket.class,
            NetworkDirection.PLAY_TO_SERVER,
            C2SChallengeReplyPacket::decode,
            (packet, contextSupplier) -> {
                NetworkEvent.Context context = contextSupplier.get();
                context.enqueueWork(() -> {
                    ServerPlayer sender = context.getSender();
                    if (sender != null) {
                        ServerAuth.handleChallengeReply(sender, packet);
                    }
                });
                context.setPacketHandled(true);
            }
        );
    }

    @Override
    public void registerClientPackets() {
    }

    @Override
    public void sendToPlayer(ServerPlayer player, NetworkPayload payload) {
        if (player == null || payload == null) {
            return;
        }
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
    }

    @Override
    public void sendToServer(NetworkPayload payload) {
        if (payload == null) {
            return;
        }
        CHANNEL.sendToServer(payload);
    }

    private static <T extends NetworkPayload> void registerMessage(
        int id,
        Class<T> type,
        NetworkDirection direction,
        Function<FriendlyByteBuf, T> decoder,
        java.util.function.BiConsumer<T, Supplier<NetworkEvent.Context>> handler
    ) {
        CHANNEL.messageBuilder(type, id, direction)
            .encoder((payload, buffer) -> payload.write(buffer))
            .decoder(decoder)
            .consumerMainThread(handler)
            .add();
    }
}
