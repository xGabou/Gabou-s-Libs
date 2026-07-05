package net.Gabou.gaboulibs.fabric;

import net.Gabou.gaboulibs.Gaboulibs;
import net.Gabou.gaboulibs.ModNetworking;
import net.Gabou.gaboulibs.fabric.auth.Identity2FabricNetworkingPlatform;
import net.fabricmc.api.ModInitializer;

public final class GaboulibsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        ModNetworking.setPlatform(new Identity2FabricNetworkingPlatform());
        // Run our common setup.
        Gaboulibs.init();
    }
}
