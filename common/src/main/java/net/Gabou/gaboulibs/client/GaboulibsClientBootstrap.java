package net.Gabou.gaboulibs.client;

import net.Gabou.gaboulibs.GaboulibsClient;

public final class GaboulibsClientBootstrap {
    private GaboulibsClientBootstrap() {
    }

    public static void initialize() {
        GaboulibsClient.initialize();
    }
}

