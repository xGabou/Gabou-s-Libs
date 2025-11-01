package net.Gabou.gaboulibs.util;

import dev.architectury.platform.Platform;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class CompatUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger("CompatUtils");

    private static final Set<String> INCOMPATIBLE_TYPES = new HashSet<>();

    public static boolean isBlacklistedEntityType(String id) {
        if (id == null) {
            return false;
        }

        if (INCOMPATIBLE_TYPES.contains(id)) {
            return true;
        }

        // Blacklist DragonMounts dragon
        return id.contains("dragonmounts");
    }

    public static void markIncompatibleEntityType(String id) {
        if (id != null) {
            INCOMPATIBLE_TYPES.add(id);
            LOGGER.warn("Marked incompatible entity type {}", id);
        }
    }

    public static boolean isAlexsMobsLoaded() {
        return Platform.isModLoaded("alexsmobs");
    }

    public static boolean isNaturalistLoaded() {
        return Platform.isModLoaded("naturalist");
    }
}
