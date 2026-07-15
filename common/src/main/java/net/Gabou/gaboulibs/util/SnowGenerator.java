package net.Gabou.gaboulibs.util;

import net.Gabou.gaboulibs.storage.SnowRecord;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class SnowGenerator {

    private static int minLayers = 1;
    private static int maxLayers = 3;

    public static SnowRecord generateStormRecord(RandomSource random) {
        int min = minLayers;
        int max = min + random.nextInt(maxLayers - minLayers + 1);

        int avg = Mth.ceil((min + max) / 2f);

        int[] distribution = new int[16];

        for (int i = 0; i < distribution.length; i++) {
            distribution[i] = min + random.nextInt(max - min + 1);
        }

        return new SnowRecord(min, avg, max, distribution);
    }

    public static int getMinLayers() {
        return minLayers;
    }

    public static int getMaxLayers() {
        return maxLayers;
    }

    /**
     * Sets the minimum amount of snow layers generated during a storm.
     *
     * @param min minimum layer count
     */
    public static void setMinLayers(int min) {
        minLayers = Math.max(1, min);

        if (maxLayers < minLayers) {
            maxLayers = minLayers;
        }
    }

    /**
     * Sets the maximum amount of snow layers generated during a storm.
     *
     * @param max maximum layer count
     */
    public static void setMaxLayers(int max) {
        maxLayers = Math.max(minLayers, max);
    }
}