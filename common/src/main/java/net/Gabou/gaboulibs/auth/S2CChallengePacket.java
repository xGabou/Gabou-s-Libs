package net.Gabou.gaboulibs.auth;

import net.Gabou.gaboulibs.ModNetworking;
import net.Gabou.gaboulibs.util.NetworkPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record S2CChallengePacket(long nonce) implements NetworkPayload {
    public static final ResourceLocation ID = ModNetworking.AUTH_CHALLENGE_PACKET_ID;
    public static final CustomPacketPayload.Type<S2CChallengePacket> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CChallengePacket> STREAM_CODEC = CustomPacketPayload.codec(
        S2CChallengePacket::write,
        S2CChallengePacket::decode
    );

    public static S2CChallengePacket decode(FriendlyByteBuf buffer) {
        return new S2CChallengePacket(buffer.readLong());
    }

    @Override
    public CustomPacketPayload.Type<S2CChallengePacket> type() {
        return TYPE;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeLong(nonce);
    }
}
