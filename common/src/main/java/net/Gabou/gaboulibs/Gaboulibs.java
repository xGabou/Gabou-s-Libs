package net.Gabou.gaboulibs;

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import net.Gabou.gaboulibs.auth.ServerAuth;
import net.Gabou.gaboulibs.auth.ReplayCompatibility;
import net.Gabou.gaboulibs.util.CompatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Gaboulibs {
    public static final String MOD_ID = "gaboulibs";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        CompatUtils.logProjectAtmosphereSereneSeasonsPlusStatus();
        ReplayCompatibility.logModStatus();
        TickEvent.LevelTick.SERVER_POST.register(ServerAuth::onTick);
        PlayerEvent.PLAYER_JOIN.register(ServerAuth::onLogin);
        PlayerEvent.PLAYER_QUIT.register(ServerAuth::onLogout);
        ModNetworking.init();
    }


}

