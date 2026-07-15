package net.Gabou.gaboulibs.util;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;


public interface NetworkPayload extends CustomPacketPayload {
    @Override
    Type<? extends NetworkPayload> type();

    default Identifier id() {
        return type().id();
    }

    void write(FriendlyByteBuf buffer);
}
