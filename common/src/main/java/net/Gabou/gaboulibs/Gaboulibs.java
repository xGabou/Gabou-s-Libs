package net.Gabou.gaboulibs;

import net.Gabou.gaboulibs.util.CompatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Gaboulibs {
    public static final String MOD_ID = "gaboulibs";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        CompatUtils.logProjectAtmosphereSereneSeasonsPlusStatus();
    }


}

