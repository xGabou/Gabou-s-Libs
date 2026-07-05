package net.Gabou.gaboulibs.client;

import net.Gabou.gaboulibs.GaboulibsClient;
import net.Gabou.gaboulibs.client.platform.ModClientPlatform;

public final class GaboulibsClientBootstrap {
    private GaboulibsClientBootstrap() {
    }

    public static void initialize() {
        GaboulibsClient.initialize();
    }
}

