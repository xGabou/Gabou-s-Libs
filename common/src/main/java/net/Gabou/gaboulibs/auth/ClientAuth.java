package net.Gabou.gaboulibs.auth;

import net.Gabou.gaboulibs.ModNetworking;
import net.minecraft.client.Minecraft;

public final class ClientAuth {
    private ClientAuth() {
    }

    public static void handleChallenge(S2CChallengePacket packet) {
        if (packet == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.player == null) {
            return;
        }

        ClientLauncherGuards.enforce();
        ModNetworking.sendToServer(new C2SChallengeReplyPacket(
            packet.nonce(),
            SharedSecret.computeResponse(minecraft.player.getUUID(), packet.nonce()),
            ClientLauncherGuards.getDetectedReason()
        ));
    }
}
