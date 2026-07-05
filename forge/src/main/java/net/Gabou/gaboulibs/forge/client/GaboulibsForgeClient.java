package net.Gabou.gaboulibs.forge.client;

import net.Gabou.gaboulibs.client.GaboulibsClientBootstrap;

public class GaboulibsForgeClient {
    private static boolean initialized = false;

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        GaboulibsClientBootstrap.initialize();
    }
}
