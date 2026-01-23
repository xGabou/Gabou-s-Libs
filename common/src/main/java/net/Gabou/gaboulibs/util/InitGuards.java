package net.Gabou.gaboulibs.util;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class InitGuards {
    private static final Set<String> STARTED_KEYS = ConcurrentHashMap.newKeySet();

    private InitGuards() {
    }

    public static boolean tryStart(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        return STARTED_KEYS.add(key);
    }
}
