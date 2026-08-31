package net.Gabou.gaboulibs.neoforge;

import net.Gabou.gaboulibs.Gaboulibs;
import net.Gabou.gaboulibs.neoforge.client.GaboulibsNeoForgeClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(Gaboulibs.MOD_ID)
public final class GaboulibsForge {
    public GaboulibsForge() {
        // Run our common setup.
        Gaboulibs.init();
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            GaboulibsNeoForgeClient.initialize();
        }
    }
}
