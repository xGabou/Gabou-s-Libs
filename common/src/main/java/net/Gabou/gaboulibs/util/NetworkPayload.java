package net.Gabou.gaboulibs.util;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public interface NetworkPayload extends CustomPacketPayload {
    @Override
    Type<? extends NetworkPayload> type();

    default ResourceLocation id() {
        return type().id();
    }

    void write(FriendlyByteBuf buffer);
}
