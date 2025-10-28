package net.Gabou.gaboulibs.forge;

import net.Gabou.gaboulibs.Gaboulibs;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Gaboulibs.MOD_ID)
public final class GaboulibsForge {
    public GaboulibsForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(Gaboulibs.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // Run our common setup.
        Gaboulibs.init();
    }
}
