package net.Gabou.gaboulibs.neoforge.client;

import net.Gabou.gaboulibs.client.GaboulibsClientBootstrap;

public class GaboulibsNeoForgeClient {
    private static boolean initialized = false;

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        GaboulibsClientBootstrap.initialize();
    }
}
