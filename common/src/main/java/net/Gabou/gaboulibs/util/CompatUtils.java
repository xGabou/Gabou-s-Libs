package net.Gabou.gaboulibs.util;

import dev.architectury.platform.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class CompatUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger("CompatUtils");

    private static final Set<String> INCOMPATIBLE_TYPES = new HashSet<>();
    private static final String PROJECT_ATMOSPHERE_MOD_ID = "projectatmosphere";
    private static final String SERENE_SEASONS_PLUS_MOD_ID = "sereneseasonsplus";
    private static final String PA_SSP_HOST_PROPERTY = "gaboulibs.pasphost";
    private static final AtomicBoolean PA_SSP_OVERRIDE_LOGGED = new AtomicBoolean(false);

    public enum PaSspHost {
        NONE,
        PROJECT_ATMOSPHERE,
        SERENE_SEASONS_PLUS
    }

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

    public static boolean isProjectAtmosphereLoaded() {
        return Platform.isModLoaded(PROJECT_ATMOSPHERE_MOD_ID);
    }

    public static boolean isSereneSeasonsPlusLoaded() {
        return Platform.isModLoaded(SERENE_SEASONS_PLUS_MOD_ID);
    }

    public static boolean areProjectAtmosphereAndSereneSeasonsPlusLoaded() {
        return isProjectAtmosphereLoaded() && isSereneSeasonsPlusLoaded();
    }

    public static PaSspHost getPaSspHost() {
        boolean paLoaded = isProjectAtmosphereLoaded();
        boolean sspLoaded = isSereneSeasonsPlusLoaded();

        if (paLoaded && sspLoaded) {
            String overrideValue = System.getProperty(PA_SSP_HOST_PROPERTY);
            PaSspHost overridden = parsePaSspHostOverride(overrideValue);
            if (overridden != null) {
                if (PA_SSP_OVERRIDE_LOGGED.compareAndSet(false, true)) {
                    LOGGER.info("PA/SSP host override active ({}={}): forcing host to {}.", PA_SSP_HOST_PROPERTY, overrideValue, overridden);
                }
                return overridden;
            }
            return PaSspHost.PROJECT_ATMOSPHERE;
        }

        if (paLoaded) {
            return PaSspHost.PROJECT_ATMOSPHERE;
        }

        if (sspLoaded) {
            return PaSspHost.SERENE_SEASONS_PLUS;
        }

        return PaSspHost.NONE;
    }

    public static boolean shouldThisModHostPaSspFeature(String thisModId) {
        if (thisModId == null) {
            return false;
        }

        PaSspHost host = getPaSspHost();
        if (host == PaSspHost.PROJECT_ATMOSPHERE) {
            return PROJECT_ATMOSPHERE_MOD_ID.equals(thisModId);
        }
        if (host == PaSspHost.SERENE_SEASONS_PLUS) {
            return SERENE_SEASONS_PLUS_MOD_ID.equals(thisModId);
        }
        return false;
    }

    private static PaSspHost parsePaSspHostOverride(String override) {
        if (override == null || override.isBlank()) {
            return null;
        }

        String normalized = override.trim().toLowerCase();
        String compact = normalized.replaceAll("[^a-z0-9]", "");
        if ("pa".equals(compact) || "projectatmosphere".equals(compact)) {
            return PaSspHost.PROJECT_ATMOSPHERE;
        }
        if ("ssp".equals(compact) || "sereneseasonsplus".equals(compact)) {
            return PaSspHost.SERENE_SEASONS_PLUS;
        }
        return null;
    }

    public static void logProjectAtmosphereSereneSeasonsPlusStatus() {
        PaSspHost host = getPaSspHost();
        if (areProjectAtmosphereAndSereneSeasonsPlusLoaded()) {
            LOGGER.warn("Detected both Project Atmosphere and Serene Seasons Plus; running with both may be unstable. Elected host: {}", host);
        } else if (isProjectAtmosphereLoaded()) {
            LOGGER.info("Detected Project Atmosphere. Elected host: {}", host);
        } else if (isSereneSeasonsPlusLoaded()) {
            LOGGER.info("Detected Serene Seasons Plus. Elected host: {}", host);
        }
    }
}
