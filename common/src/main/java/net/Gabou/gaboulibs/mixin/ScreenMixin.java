package net.Gabou.gaboulibs.mixin;

import net.Gabou.gaboulibs.util.IScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

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

    @Shadow
    protected abstract void extractPanorama(GuiGraphicsExtractor guiGraphics, float f);

    @Shadow
    protected abstract void extractMenuBackground(GuiGraphicsExtractor guiGraphics);

    /**
     * Renders all widgets without drawing any background.
     */
    @Unique
    public void sereneseasonsplus$renderNoBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        sereneseasonsplus$renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        for (Renderable renderable : this.renderables) {
            renderable.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    /**
     * Renders the blurred background + panorama when needed.
     */
    @Unique
    public void sereneseasonsplus$renderBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.minecraft.level == null) {
            this.extractPanorama(guiGraphics, partialTick);
        }

        this.extractMenuBackground(guiGraphics);
    }

    @Unique
    protected void sereneseasonsplus$renderBlurredBackground(GuiGraphicsExtractor guiGraphics) {
        float f = (float) this.minecraft.options.getMenuBackgroundBlurriness();
        if (f >= 1.0F) {
            guiGraphics.blurBeforeThisStratum();
        }
    }
}
