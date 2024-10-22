package com.cyao.animatedLogo.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.resource.ResourceReload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashOverlay.class)
public class SplashOverlayMixin {
    @Shadow @Final private ResourceReload reload;
    @Shadow private float progress;
    @Unique
    private int count = 0;
    @Unique
    private Identifier[] frames;
    @Unique
    private boolean inited = false;
    @Unique
    private static final int FRAMES = 12;
    @Unique
    private static final int IMAGE_PER_FRAME = 4;
    @Unique
    private static final int FRAMES_PER_FRAME = 3;
    @Unique
    private float f = 0;

    @ModifyArg(method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIFFIIII)V", ordinal = 0),
            index = 3
    )
    private int removeText1(int i) {
        return 0;
    }
    @ModifyArg(method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIFFIIII)V", ordinal = 1),
            index = 3
    )
    private int removeText2(int i) {
        return 0;
    }

    /**
     * Draws the logo shadows
     * Original names for future reference:
     * @param scaledWidth i
     * @param scaledHeight j
     * @param now l
     * @param fadeOutProgress f
     * @param fadeInProgress g
     * @param alpha s
     * @param x t
     * @param y u
     * @param height d
     * @param halfHeight v
     * @param width e
     * @param halfWidth w
     */
    @Inject(method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIFFIIII)V", ordinal = 1, shift = At.Shift.AFTER)
    )
    private void onAfterRenderLogo(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci, @Local(ordinal = 2) int scaledWidth, @Local(ordinal = 3) int scaledHeight, @Local long now, @Local(ordinal = 1) float fadeOutProgress, @Local(ordinal = 2) float fadeInProgress, @Local(ordinal = 3) float alpha, @Local(ordinal = 4) int x, @Local(ordinal = 5) int y, @Local(ordinal = 0) double height, @Local(ordinal = 6) int halfHeight, @Local(ordinal = 1) double width, @Local(ordinal = 7) int halfWidth) {
        if (!inited) {
            this.frames = new Identifier[FRAMES];

            for (int i = 0; i < FRAMES; i++) {
                this.frames[i] = Identifier.of("animated-logo", "textures/gui/frame_" + i + ".png");
            }

            inited = true;
        }

        float progress = MathHelper.clamp(this.progress * 0.95F + this.reload.getProgress() * 0.050000012F, 0.0F, 1.0F);

        context.drawTexture(this.frames[count / IMAGE_PER_FRAME / FRAMES_PER_FRAME], x - halfWidth, y - halfHeight, (int) width, (int)height,
                0, 256 * ((count % (IMAGE_PER_FRAME * FRAMES_PER_FRAME)) / FRAMES_PER_FRAME), 1024, 256, 1024, 1024);

        if (progress >= 0.8) {
            f = Math.min(alpha, f + 0.2f);

            context.setShaderColor(1.0F, 1.0F, 1.0F, f);
            context.drawTexture(Identifier.of("animated-logo", "textures/gui/studios.png"), x - halfWidth / 2, (int) (y - halfHeight + height), (int) (width / 2), (int) (height / 5),
                    0, 0, 450, 50, 512, 512);
        }

	// Uncomment to loop animation
        if (/* progress <= 0.8 || */ count != FRAMES * IMAGE_PER_FRAME * FRAMES_PER_FRAME - 1) {
            count++;
	    /*
            if (count >= FRAMES * IMAGE_PER_FRAME * FRAMES_PER_FRAME) {
                count = 0;
            }
	    */
        }
    }
}
