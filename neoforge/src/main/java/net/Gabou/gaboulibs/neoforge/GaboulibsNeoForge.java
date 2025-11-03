package net.Gabou.gaboulibs.neoforge;

import net.Gabou.gaboulibs.Gaboulibs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Gaboulibs.MOD_ID)
public final class GaboulibsNeoForge {
    public GaboulibsNeoForge(IEventBus modEventBus) {
        Gaboulibs.init();
    }
}
