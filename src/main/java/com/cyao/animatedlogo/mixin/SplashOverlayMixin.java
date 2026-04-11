package com.cyao.animatedlogo.mixin;

import com.cyao.animatedlogo.AnimatedLogo;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
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
import net.minecraft.sounds.SoundSource;
import java.io.BufferedInputStream;
import java.io.InputStream;

//? fabric {
import net.minecraft.client.gui.screens.LoadingOverlay;
//? }

//? neoforge {
import net.neoforged.neoforge.client.loading.NeoForgeLoadingOverlay;
//? }

//? fabric
//@Mixin(LoadingOverlay.class)
//? neoforge
@Mixin(NeoForgeLoadingOverlay.class)
public class SplashOverlayMixin {
    @Shadow
    @Final
    private ReloadInstance reload;
    @Shadow
    private float currentProgress;

	//? fabric {
	/*@Shadow
	private long fadeOutStart;
	@Shadow
	private long fadeInStart = -1L;
	*///? }

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

    @ModifyArg(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIIIIII)V", ordinal = 0),
            index = 6
    )
    private int removeText1(int i) {
        return 0;
    }

    @ModifyArg(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIIIIII)V", ordinal = 1),
            index = 6
    )
    private int removeText2(int u) {
        return 0;
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIIIIII)V", ordinal = 1, shift = At.Shift.AFTER)
    )
    private void onAfterRenderLogo(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci,
                                   @Local(name = "logoAlpha") float logoAlpha,
                                   @Local(name = "contentX") int contentX,
                                   @Local(name = "contentWidth") double contentWidth,
                                   @Local(name = "logoY") int logoY,
                                   @Local(name = "logoHeight") double logoHeight,
                                   @Local(name = "logoHeightHalf") int logoHeightHalf,
                                   @Local(name = "logoWidthHalf") int logoWidthHalf) {
        if (!inited) {
            this.frames = new Identifier[FRAMES];

            for (int i = 0; i < FRAMES; i++) {
                this.frames[i] = Identifier.fromNamespaceAndPath(AnimatedLogo.MOD_ID, "textures/gui/frame_" + i + ".png");
            }

            if (!reload.isDone()) {
                final Minecraft client = Minecraft.getInstance();
                final ResourceManager resourceManager = client.getResourceManager();
                final Identifier soundId = Identifier.fromNamespaceAndPath(AnimatedLogo.MOD_ID, "logo.wav");

                resourceManager.getResource(soundId).ifPresent(resource -> new Thread(() -> {
                    try (final InputStream inputStream = resource.open(); final BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream); final AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream)) {
                        final Clip clip = AudioSystem.getClip();
                        clip.open(audioInputStream);
                        { // Calculate gain from minecraft's master volume
                            final FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                            final float masterVolume = Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER);
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

        float actualProgress = this.reload.getActualProgress();
        float progress = Mth.clamp(this.currentProgress * 0.95F + actualProgress * 0.050000012F, 0.0F, 1.0F);

        graphics.blit(
                RenderPipelines.MOJANG_LOGO, this.frames[count / IMAGE_PER_FRAME / FRAMES_PER_FRAME], contentX - logoWidthHalf, logoY - logoHeightHalf,
                0, 256 * ((count % (IMAGE_PER_FRAME * FRAMES_PER_FRAME)) / FRAMES_PER_FRAME), (int) contentWidth, (int) logoHeight, 1024, 256, 1024, 1024, ARGB.white(logoAlpha)
        );

        if (progress >= 0.8) {
            f = Math.min(logoAlpha, f + 0.2f);

            int sw = (int) (contentWidth * 0.45);
            graphics.blit(
                    RenderPipelines.MOJANG_LOGO, Identifier.fromNamespaceAndPath(AnimatedLogo.MOD_ID, "textures/gui/studios.png"), contentX - sw / 2, (int) (logoY - logoHeightHalf + logoHeight - logoHeight / 12),
                    0, 0, sw, (int) (logoHeight / 5.0), 450, 50, 512, 512, ARGB.white(f)
            );
        }

        if (!reload.isDone()) {
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
                this.currentProgress = 0.9f;
                this.fadeOutStart = -1L;
                this.fadeInStart = -1L;
            }
        }
    }
}
