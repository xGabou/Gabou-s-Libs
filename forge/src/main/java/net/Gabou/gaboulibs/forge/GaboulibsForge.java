package net.Gabou.gaboulibs.forge;

import net.Gabou.gaboulibs.Gaboulibs;
import dev.architectury.platform.forge.EventBuses;
import net.Gabou.gaboulibs.ModNetworking;
import net.Gabou.gaboulibs.forge.auth.Identity2ForgeNetworkingPlatform;
import net.Gabou.gaboulibs.forge.client.GaboulibsForgeClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(Gaboulibs.MOD_ID)
public final class GaboulibsForge {
    public GaboulibsForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(Gaboulibs.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        ModNetworking.setPlatform(new Identity2ForgeNetworkingPlatform());

        // Run our common setup.
        Gaboulibs.init();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            GaboulibsForgeClient.initialize();
        }
    }
}
