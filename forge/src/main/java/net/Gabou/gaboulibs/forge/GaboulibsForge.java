package net.Gabou.gaboulibs.forge;

import dev.architectury.platform.forge.EventBuses;
import net.Gabou.gaboulibs.Gaboulibs;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Gaboulibs.MOD_ID)
public final class GaboulibsForge {
    public GaboulibsForge() {
        // Submit mod event bus so Architectury + Forge lifecycle hooks run correctly.
        EventBuses.registerModEventBus(Gaboulibs.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        Gaboulibs.init();
    }
}
