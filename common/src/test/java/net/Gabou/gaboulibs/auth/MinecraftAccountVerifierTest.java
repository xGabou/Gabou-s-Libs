package net.Gabou.gaboulibs.auth;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinecraftAccountVerifierTest {
    private static final UUID ONLINE_UUID = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5");
    private static final UUID OFFLINE_UUID = UUID.fromString("b50ad385-829d-3141-a216-7e7d7539ba7f");
    private static final UUID VERSION_FIVE_UUID = UUID.fromString("21f7f8de-8051-5b89-8680-0195ef798b6a");

    @Test void authenticatedOnlineProfilePasses() {
        assertTrue(MinecraftAccountVerifier.verifyProfile(ONLINE_UUID, "RealPlayer", true).passed());
    }
    @Test void offlineModeServerFailsEvenWithVersionFourProfile() {
        McAccountResult result = MinecraftAccountVerifier.verifyProfile(ONLINE_UUID, "SpoofedPlayer", false);
        assertFalse(result.passed());
        assertTrue(result.reason().contains("online mode"));
    }
    @Test void versionThreeProfileFailsForNormalJoin() {
        McAccountResult result = MinecraftAccountVerifier.verifyProfile(OFFLINE_UUID, "OfflinePlayer", true);
        assertFalse(result.passed());
        assertTrue(result.reason().contains("version 3"));
    }
    @Test void unexpectedUuidVersionFails() {
        assertFalse(MinecraftAccountVerifier.verifyProfile(VERSION_FIVE_UUID, "Synthetic", true).passed());
    }
    @Test void missingIdentityFails() {
        assertFalse(MinecraftAccountVerifier.verifyProfile(null, "Player", true).passed());
        assertFalse(MinecraftAccountVerifier.verifyProfile(ONLINE_UUID, " ", true).passed());
    }
}
