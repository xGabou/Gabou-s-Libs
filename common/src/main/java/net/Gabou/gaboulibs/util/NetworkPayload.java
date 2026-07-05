package net.Gabou.gaboulibs.util;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface NetworkPayload {
    ResourceLocation id();

    void write(FriendlyByteBuf buffer);
}
