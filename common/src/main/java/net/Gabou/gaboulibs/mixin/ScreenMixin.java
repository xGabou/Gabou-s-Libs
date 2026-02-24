package net.Gabou.gaboulibs.mixin;


import net.Gabou.gaboulibs.util.IScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

/**
 * Adds a no-background render helper directly inside Screen.
 * Provides access for modded screens that want to reuse it.
 */
@Mixin(Screen.class)
public abstract class ScreenMixin implements IScreen {

    @Final
    @Shadow private List<Renderable> renderables;

    @Shadow protected Minecraft minecraft;

    // These are the protected methods from Screen
    @Shadow
    protected abstract void renderPanorama(GuiGraphics guiGraphics, float f);

    @Shadow
    protected abstract void renderMenuBackground(GuiGraphics guiGraphics);

    /**
     * Renders all widgets without drawing any background.
     */
    @Unique
    public void sereneseasonsplus$renderNoBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        sereneseasonsplus$renderBackground(g, mouseX, mouseY, partialTick);
        for (Renderable renderable : this.renderables) {
            renderable.render(g, mouseX, mouseY, partialTick);
        }
    }

    /**
     * Renders the blurred background + panorama when needed.
     */
    @Unique
    public void sereneseasonsplus$renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.minecraft.level == null) {
            // ✅ Just call the invoker normally — no cast needed
            this.renderPanorama(guiGraphics, partialTick);
        }

        this.renderMenuBackground(guiGraphics);
    }

    @Unique
    protected void sereneseasonsplus$renderBlurredBackground(float partialTick) {
        var renderer = this.minecraft.gameRenderer;
        try {
            renderer.getClass().getMethod("processBlurEffect", float.class).invoke(renderer, partialTick);
        } catch (ReflectiveOperationException ignored) {
            try {
                renderer.getClass().getMethod("processBlurEffect").invoke(renderer);
            } catch (ReflectiveOperationException ignoredAgain) {
                // Method signature varies across mappings/versions; skip blur if unavailable.
            }
        }
        this.minecraft.getMainRenderTarget().bindWrite(false);
    }
}
