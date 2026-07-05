package net.Gabou.gaboulibs.fabric.client;

import net.Gabou.gaboulibs.client.GaboulibsClientBootstrap;
import net.fabricmc.api.ClientModInitializer;

public final class GaboulibsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        GaboulibsClientBootstrap.initialize();
    }
}
