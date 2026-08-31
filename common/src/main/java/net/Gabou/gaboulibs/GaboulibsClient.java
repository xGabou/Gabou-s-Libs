package net.Gabou.gaboulibs;

import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.Gabou.gaboulibs.client.platform.ModClientPlatform;

public class GaboulibsClient {
    public static final GaboulibsClient INSTANCE = new GaboulibsClient();
    private static boolean initialized = false;
    private GaboulibsClient() {}

    public static void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;
    }

}
