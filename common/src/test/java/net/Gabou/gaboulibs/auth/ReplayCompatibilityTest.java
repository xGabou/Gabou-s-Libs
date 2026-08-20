package net.Gabou.gaboulibs.auth;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReplayCompatibilityTest {
    private static final UUID FLASHBACK_UUID = UUID.fromString("b50ad385-829d-3141-a216-7e7d7539ba7f");

    @Test void exactFlashbackViewerSignalsAreAccepted() {
        ReplayDetection detection = ReplayCompatibility.classifyFlashback(true, true, true, FLASHBACK_UUID, "Replay Viewer", true);
        assertTrue(detection.detected());
        assertEquals(ReplayEnvironment.FLASHBACK, detection.environment());
    }
    @Test void versionThreeUuidAloneIsNeverAccepted() {
        assertFalse(ReplayCompatibility.classifyFlashback(false, false, false, FLASHBACK_UUID, "OfflinePlayer", false).detected());
    }
    @Test void everyFlashbackSignalIsRequired() {
        assertFalse(ReplayCompatibility.classifyFlashback(false, true, true, FLASHBACK_UUID, "Replay Viewer", true).detected());
        assertFalse(ReplayCompatibility.classifyFlashback(true, false, true, FLASHBACK_UUID, "Replay Viewer", true).detected());
        assertFalse(ReplayCompatibility.classifyFlashback(true, true, false, FLASHBACK_UUID, "Replay Viewer", true).detected());
        assertFalse(ReplayCompatibility.classifyFlashback(true, true, true, FLASHBACK_UUID, "Other", true).detected());
        assertFalse(ReplayCompatibility.classifyFlashback(true, true, true, FLASHBACK_UUID, "Replay Viewer", false).detected());
        assertFalse(ReplayCompatibility.classifyFlashback(true, true, true, UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5"), "Replay Viewer", true).detected());
    }
    @Test void replayModMustBeInstalledAndActivelyReplaying() {
        assertTrue(ReplayModCompatibility.shouldSuppressRecordedChallenge(true, true));
        assertFalse(ReplayModCompatibility.shouldSuppressRecordedChallenge(true, false));
        assertFalse(ReplayModCompatibility.shouldSuppressRecordedChallenge(false, true));
    }
}
