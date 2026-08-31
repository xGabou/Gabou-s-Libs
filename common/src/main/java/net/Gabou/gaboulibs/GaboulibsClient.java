package net.Gabou.gaboulibs;


public class GaboulibsClient {
    public static final GaboulibsClient INSTANCE = new GaboulibsClient();
    private static boolean initialized = false;
    private GaboulibsClient() {}

    public static void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;
    }

}
