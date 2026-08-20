package net.Gabou.gaboulibs.auth;

import dev.architectury.platform.Platform;
import net.Gabou.gaboulibs.Gaboulibs;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ReplayModCompatibility {
    private static final String REPLAY_MODULE_CLASS = "com.replaymod.replay.ReplayModReplay";
    private static boolean activePathLogged;
    private static boolean reflectionFailureLogged;

    private ReplayModCompatibility() {
    }

    public static boolean isReplayActive() {
        boolean modLoaded = Platform.isModLoaded(ReplayCompatibility.REPLAYMOD_MOD_ID);
        if (!modLoaded) return false;
        try {
            Class<?> moduleClass = Class.forName(REPLAY_MODULE_CLASS, false, ReplayModCompatibility.class.getClassLoader());
            Field instanceField = moduleClass.getField("instance");
            Object instance = instanceField.get(null);
            if (instance == null) return false;
            Method getReplayHandler = moduleClass.getMethod("getReplayHandler");
            return shouldSuppressRecordedChallenge(modLoaded, getReplayHandler.invoke(instance) != null);
        } catch (ReflectiveOperationException | LinkageError exception) {
            if (!reflectionFailureLogged) {
                reflectionFailureLogged = true;
                Gaboulibs.LOGGER.debug("ReplayMod was detected but its active replay state could not be inspected.", exception);
            }
            return false;
        }
    }

    static boolean shouldSuppressRecordedChallenge(boolean modLoaded, boolean replayHandlerActive) {
        return modLoaded && replayHandlerActive;
    }

    public static void logActiveCompatibilityPath() {
        if (!activePathLogged) {
            activePathLogged = true;
            Gaboulibs.LOGGER.info("Replay identity detected; using ReplayMod embedded-channel compatibility path.");
        }
    }
}
