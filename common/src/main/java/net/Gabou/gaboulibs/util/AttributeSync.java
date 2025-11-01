package net.Gabou.gaboulibs.util;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class AttributeSync {

    public static void syncMaxHealth(ServerPlayer player) {
        if (player == null) return;

        double maxHealth = player.getAttributeValue(Attributes.MAX_HEALTH);
        player.setHealth(Math.min(player.getHealth(), (float) maxHealth));

        AttributeInstance instance = player.getAttributes().getInstance(Attributes.MAX_HEALTH);
        if (instance == null) return;

        ClientboundUpdateAttributesPacket packet = new ClientboundUpdateAttributesPacket(
                player.getId(),
                List.of(instance)
        );

        player.connection.send(packet);

        ServerLevel level = player.serverLevel();
        level.getChunkSource().broadcast(player, packet);
    }
}
