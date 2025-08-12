package com.cyao.animatedLogo.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReload;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.BufferedInputStream;
import java.io.InputStream;

@Mixin(SplashOverlay.class)
public class SplashOverlayMixin {

    @Shadow
    @Final
    private ResourceReload reload;
    @Shadow
    private long reloadCompleteTime = -1L;
    @Shadow
    private long reloadStartTime = -1L;
    @Shadow
    private float progress;
    @Shadow
    @Final
    private boolean reloading;
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
    private static final int FRAMES_PER_FRAME = 2;
    @Unique
    private float f = 0;
    @Unique
    private boolean fast = false;
    @Unique
    private boolean done = false;
    @Unique
    private boolean animDone = false;
    @Unique
    private long startTime;

    @ModifyArg(method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIFFIIIIIII)V", ordinal = 0),
            index = 7
    )
    private int removeText1(int i) {
        return 0;
    }

    @ModifyArg(method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIFFIIIIIII)V", ordinal = 1),
            index = 7
    )
    private int removeText2(int u) {
        return 0;
    }

    /**
     * Draws the logo shadows
     * Original names for future reference:
     * @param scaledWidth i
     * @param scaledHeight j
     * @param alpha s
     * @param x t
     * @param y u
     * @param height d
     * @param halfHeight v
     * @param width e
     * @param halfWidth w
     */
    @Inject(method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIFFIIIIIII)V", ordinal = 1, shift = At.Shift.AFTER)
    )
    private void onAfterRenderLogo(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci,
                                   @Local(ordinal = 2) int scaledWidth, @Local(ordinal = 3) int scaledHeight, @Local(ordinal = 3) float alpha, @Local(ordinal = 4) int x, @Local(ordinal = 5) int y, @Local(ordinal = 0) double height, @Local(ordinal = 6) int halfHeight, @Local(ordinal = 1) double width, @Local(ordinal = 7) int halfWidth) {
        if (!inited) {
            this.frames = new Identifier[FRAMES];

            for (int i = 0; i < FRAMES; i++) {
                this.frames[i] = Identifier.of("animated-logo", "textures/gui/frame_" + i + ".png");
            }

            if (!reloading) {
                final MinecraftClient client = MinecraftClient.getInstance();
                final ResourceManager resourceManager = client.getResourceManager();
                final Identifier soundId = Identifier.of("animated-logo", "logo.wav");

                resourceManager.getResource(soundId).ifPresent(resource -> new Thread(() -> {
                    try (final InputStream inputStream = resource.getInputStream(); final BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream); final AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream)) {
                        final Clip clip = AudioSystem.getClip();
                        clip.open(audioInputStream);
                        { // Calculate gain from minecraft's master volume
                            final FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                            final float masterVolume = MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.MASTER);
                            gainControl.setValue(masterVolume > 0.0 ? Math.min(gainControl.getMaximum(), Math.max(gainControl.getMinimum(), (float) (Math.log10(masterVolume) * 20.0))) : gainControl.getMinimum());
                        }
                        this.startTime = System.currentTimeMillis();
                        clip.start();
                    } catch (final Exception ignored) {
                    }
                }, "SoundPlayer").start());
            }

            inited = true;
            startTime = System.currentTimeMillis();
        }

        if (count == 0) {
            fast = false;
            done = false;
            animDone = false;
        }

        float progress = MathHelper.clamp(this.progress * 0.95F + this.reload.getProgress() * 0.050000012F, 0.0F, 1.0F);
        
        context.drawTexture(
                RenderLayer::getGuiTextured, this.frames[count / IMAGE_PER_FRAME / FRAMES_PER_FRAME], x - halfWidth, y - halfHeight,
                0, 256 * ((count % (IMAGE_PER_FRAME * FRAMES_PER_FRAME)) / FRAMES_PER_FRAME), (int) width, (int) height, 1024, 256, 1024, 1024, ColorHelper.getWhite(alpha)
        );

        if (progress >= 0.8) {
            f = Math.min(alpha, f + 0.2f);

            int sw = (int) (width * 0.45);
            context.drawTexture(
                    RenderLayer::getGuiTextured, Identifier.of("animated-logo", "textures/gui/studios.png"), x - sw / 2, (int) (y - halfHeight + height - height / 12),
                    0, 0, sw, (int) (height / 5.0), 450, 50, 512, 512, ColorHelper.getWhite(f)
            );
        }

        if (reloading) {
            if (count != FRAMES * IMAGE_PER_FRAME * FRAMES_PER_FRAME - 1) {
                count++;

                if ((fast || (progress >= 0.6 && count < (FRAMES * IMAGE_PER_FRAME * FRAMES_PER_FRAME) / 2))) {
                    // Increase speed
                    if (count != FRAMES * IMAGE_PER_FRAME * FRAMES_PER_FRAME - 1) {
                        count++;
                    }
                    fast = true;
                }
            } else {
                animDone = true;
            }
        } else {
            count = Math.toIntExact((System.currentTimeMillis() - startTime) / 30);
            if (count > FRAMES * IMAGE_PER_FRAME * FRAMES_PER_FRAME - 1) {
                count = FRAMES * IMAGE_PER_FRAME * FRAMES_PER_FRAME - 1;
                animDone = true;
            }

            if (progress > 0.9f) {
                done = true;
            }

            if (done && !animDone) {
                this.progress = 0.9f;
                this.reloadCompleteTime = -1L;
                this.reloadStartTime = -1L;
            }
        }
    }
}
