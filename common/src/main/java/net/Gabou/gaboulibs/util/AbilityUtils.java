
package net.Gabou.gaboulibs.util;







import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class AbilityUtils {

    public static EntityHitResult raycastEntities(Player player, double maxDistance) {
        Vec3 eyePosition = player.getEyePosition();
        Vec3 viewVector = player.getViewVector(1.0F);
        Vec3 targetPosition = eyePosition.add(viewVector.scale(maxDistance));

        HitResult blockHit = player.level().clip(
                new ClipContext(
                        eyePosition,
                        targetPosition,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        player
                )
        );

        double blockDistance = blockHit.getType() == HitResult.Type.BLOCK
                ? eyePosition.distanceTo(blockHit.getLocation())
                : maxDistance;

        EntityHitResult closestEntityHit = null;
        double closestDistance = blockDistance;

        AABB searchBox = player.getBoundingBox()
                .expandTowards(viewVector.scale(maxDistance))
                .inflate(1.0D);

        List<Entity> entities = player.level().getEntities(
                player,
                searchBox,
                e -> e.isPickable() && e instanceof LivingEntity
        );

        for (Entity entity : entities) {
            AABB box = entity.getBoundingBox().inflate(entity.getPickRadius());
            Optional<Vec3> intersection = box.clip(eyePosition, targetPosition);

            if (intersection.isPresent()) {
                double dist = eyePosition.distanceTo(intersection.get());
                if (dist < closestDistance) {
                    closestEntityHit = new EntityHitResult(entity, intersection.get());
                    closestDistance = dist;
                }
            }
        }

        return closestEntityHit;
    }

    public static List<LivingEntity> raycastNearbyEntities(Player player, double maxDistance) {
        Vec3 eyePosition = player.getEyePosition();
        Vec3 viewVector = player.getViewVector(1.0F);

        AABB box = player.getBoundingBox()
                .expandTowards(viewVector.scale(maxDistance))
                .inflate(1.5D);

        return player.level().getEntitiesOfClass(
                LivingEntity.class,
                box,
                entity -> entity != player && entity.isPickable()
        );
    }


    public static void knockbackNearbyEntities(Player player, float radius, double strength) {
        Level world = player.level();

        for (LivingEntity living : world.getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(radius),
                e -> e != player
        )) {
            double dx = living.getX() - player.getX();
            double dz = living.getZ() - player.getZ();
            double distance = Math.max(Math.sqrt(dx * dx + dz * dz), 0.001D);

            living.setDeltaMovement(
                    dx / distance * strength,
                    0.1D,
                    dz / distance * strength
            );
            living.hurtMarked = true;
        }
    }


    public static void dashForward(Player player, double distance) {
        Vec3 look = player.getViewVector(1.0F);
        Vec3 current = player.getDeltaMovement();

        player.setDeltaMovement(
                look.x * distance,
                current.y,
                look.z * distance
        );
        player.hurtMarked = true;
    }



    public static void dropRandomItemFromInventory(Player player) {
        if (!player.getInventory().isEmpty()) {
            Random random = new Random();
            int slot = random.nextInt(player.getInventory().getContainerSize());
            ItemStack stack = player.getInventory().getItem(slot);

            if (!stack.isEmpty()) {
                ItemStack dropped = stack.split(1);
                player.drop(dropped, false);
            }
        }
    }


    public static void healNearbyPlayers(Player player, float radius, float healAmount) {
        Level level = player.level();
        for (LivingEntity living : level.getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(radius),
                e -> e instanceof Player
        )) {
            living.heal(healAmount);
        }
    }


    public static void randomMorphNearby(Player player) {
        System.out.println("Morphing into a nearby entity");
    }


    public static void constrictNearby(Player player, float radius) {
        Level level = player.level();
        List<LivingEntity> entities = level.getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(radius),
                e -> e != player
        );

        for (LivingEntity living : entities) {
            living.setDeltaMovement(Vec3.ZERO);
            living.setNoGravity(true);
            living.hurtMarked = true;
        }
    }


    public static void dashUpward(Player player, double power) {
        Vec3 current = player.getDeltaMovement();
        player.setDeltaMovement(current.x, power, current.z);
        player.hurtMarked = true;
    }


    public static void waterDash(Player player, double power) {
        if (player.isInWater()) {
            Vec3 look = player.getViewVector(1.0F);
            Vec3 current = player.getDeltaMovement();
            Vec3 added = new Vec3(look.x * power, look.y * 0.5D, look.z * power);

            player.setDeltaMovement(current.add(added));
            player.hurtMarked = true;
        }
    }


    public static void shortTeleportForward(Player player, double distance) {
        Vec3 look = player.getViewVector(1.0F);
        Vec3 target = player.position().add(look.scale(distance));
        player.teleportTo(target.x, target.y, target.z);
    }



    public static void pullEntityTowardPlayer(Player player, LivingEntity target, double strength) {
        Vec3 direction = player.position().subtract(target.position()).normalize();
        target.setDeltaMovement(
                direction.x * strength,
                0.2D,
                direction.z * strength
        );
        target.hurtMarked = true;
    }


    public static void poisonNearbyEnemies(Player player, float radius, int durationTicks, int amplifier) {
        Level level = player.level();
        for (LivingEntity entity : level.getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(radius),
                e -> e != player
        )) {
            entity.addEffect(new MobEffectInstance(MobEffects.POISON, durationTicks, amplifier));
        }
    }

    /**
     * Finds the first JukeboxBlockEntity near the origin using a 3.46-block Euclidean radius,
     * matching the behavior used in Alex's Mobs.
     *
     * @param level  The world to search in
     * @param origin The central position (e.g., player or mob)
     * @return A nearby JukeboxBlockEntity or null if none are found
     */
    public static BlockPos findNearbyJukebox(Level level, BlockPos origin) {
        double radius = 3.46;
        int blockRadius = (int) Math.ceil(radius);

        // Convert to chunk coordinates
        int originChunkX = origin.getX() >> 4;
        int originChunkZ = origin.getZ() >> 4;
        int chunkRadius = (blockRadius >> 4) + 1;

        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                ChunkAccess chunk = level.getChunk(originChunkX + dx, originChunkZ + dz);

                if (chunk instanceof LevelChunk levelChunk) {
                    for (BlockEntity be : levelChunk.getBlockEntities().values()) {
                        if (be instanceof JukeboxBlockEntity jukebox) {
                            BlockPos pos = jukebox.getBlockPos();
                            if (pos.closerThan(origin, radius)) {
                                return pos;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }


}
