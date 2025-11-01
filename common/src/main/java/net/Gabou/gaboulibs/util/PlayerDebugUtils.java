package net.Gabou.gaboulibs.util;


import net.minecraft.world.entity.player.Player;

public class PlayerDebugUtils {

    public static void logPlayerDebug(Player player, String side) {
        var bb = player.getBoundingBox();
        var dim = player.getDimensions(player.getPose());

        System.out.println("[" + side.toUpperCase() + "] Pose: " + player.getPose());
        System.out.println("[" + side.toUpperCase() + "] Sneaking: " + player.isCrouching());
        System.out.println("[" + side.toUpperCase() + "] Dimensions: " + dim.width + " x " + dim.height);
        System.out.println("[" + side.toUpperCase() + "] BoundingBox: " + bb.minX + ", " + bb.minY + ", " + bb.minZ + " -> " + bb.maxX + ", " + bb.maxY + ", " + bb.maxZ);
        System.out.println("[" + side.toUpperCase() + "] Position: " + player.getX() + ", " + player.getY() + ", " + player.getZ());
        System.out.println("------------------------------------");
    }
}