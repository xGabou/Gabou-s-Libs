package net.Gabou.gaboulibs;

import net.Gabou.gaboulibs.auth.ClientLauncherGuards;

public class GaboulibsClient {
    public static final GaboulibsClient INSTANCE = new GaboulibsClient();
    private static boolean initialized = false;
    private GaboulibsClient() {}

    public static void initialize() {
        if (initialized) {
            return;
        }

        ClientLauncherGuards.enforce();
        ModNetworking.initClient();
        initialized = true;
    }

}
