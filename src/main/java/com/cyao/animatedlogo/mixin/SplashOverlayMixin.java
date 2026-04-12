package com.cyao.animatedlogo.mixin;

import com.cyao.animatedlogo.AnimatedLogo;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.neoforged.fml.earlydisplay.DisplayWindow;
import net.neoforged.fml.loading.progress.ProgressMeter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? >= 1.21.2 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB32;
*///? }

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import net.minecraft.sounds.SoundSource;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

//? neoforge {
import net.neoforged.neoforge.client.loading.NeoForgeLoadingOverlay;
//? }

//? 1.21
import net.minecraft.util.FastColor.ARGB32;

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
	@Shadow
	private long fadeOutStart;

	//? fabric {
	/*@Shadow
	private long fadeInStart;
	@Shadow
	private boolean fadeIn;
	*///? }

	//? neoforge {
	@Final
	@Shadow
	private ProgressMeter progressMeter;
	@Final
	@Shadow
	private Consumer<Optional<Throwable>> onFinish;
	@Final
	@Shadow
	private DisplayWindow displayWindow;
	//? }

    @Unique
    private int count = 0;
    @Unique
    private ResourceLocation[] frames;
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

	//? fabric || (neoforge && >=1.21.2) {
	/*@ModifyArg(
			//? >= 26.1
			//method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
			//? 1.21
			method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
			//? >= 26.1
            //at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIFFIIIIIII)V", ordinal = 0),
			//? 1.21
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIFFIIII)V", ordinal = 0),
			//? >= 1.21.2
			//index = 6
			//? 1.21
            index = 3
    )
    private int removeText1(int i) {
        return 0;
    }
	*///? }

	//? fabric {
    /*@ModifyArg(
			//? >= 26.1
			//method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
			//? 1.21
			method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
			//? >= 26.1
			//at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIFFIIIIIII)V", ordinal = 1),
			//? 1.21
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIFFIIII)V", ordinal = 1),
			//? >= 1.21.2
			//index = 6
			//? 1.21
			index = 3
    )
    private int removeText2(int u) {
        return 0;
    }
    *///? }

	@Unique
	private static int animated_logo$replaceAlpha(int color, int alpha) {
		return color & 16777215 | alpha << 24;
	}

    @Inject(
			//? >= 26.1
			//method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
			//? 1.21
			method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
			//? 1.21 && neoforge
			cancellable = true,
			//? >= 26.1
			//at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIFFIIIIIII)V", ordinal = 0),
			//? 1.21 && fabric
			//at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIFFIIII)V", ordinal = 0, shift = At.Shift.AFTER)
			//? 1.21 && neoforge
			at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableBlend()V", ordinal = 0)
    )
	//? 26.1
	//private void onAfterRenderLogo(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci/*? neoforge { */, @Local(name = "fade") float logoAlpha/*? } */)
	//? 1.21
    private void onAfterRenderLogo(GuiGraphics graphics, int mouseX, int mouseY, float a, CallbackInfo ci
								   /*? neoforge { */, @Local(name = "fade") float logoAlpha/*? } */
	) {
		int width = graphics.guiWidth();
		int height = graphics.guiHeight();
		int contentX = (int) (width * 0.5F);
		int logoY = (int) (height * 0.5F);
		double logoHeight = Math.min(width * 0.75F, height) * 0.25F;
		int logoHeightHalf = (int) (logoHeight * 0.5F);
		double logoWidth = logoHeight * (double)4.0F;
		int logoWidthHalf = (int)(logoWidth * (double)0.5F);

		//? fabric {
		/*long now = Util.getMillis();
		if (this.fadeIn && this.fadeInStart == -1L) {
			this.fadeInStart = now;
		}

		float logoAlpha;
		float fadeOutAnim = this.fadeOutStart > -1L ? (float)(now - this.fadeOutStart) / 1000.0F : -1.0F;
		float fadeInAnim = this.fadeInStart > -1L ? (float)(now - this.fadeInStart) / 500.0F : -1.0F;
		if (fadeOutAnim >= 1.0F) {
			logoAlpha = 1.0F - Mth.clamp(fadeOutAnim - 1.0F, 0.0F, 1.0F);
		} else if (this.fadeIn) {
			logoAlpha = Mth.clamp(fadeInAnim, 0.0F, 1.0F);
		} else {
			logoAlpha = 1.0F;
		}
		*///? }

		//? neoforge {
		int LOGO_BACKGROUND_COLOR = ARGB32.color(255, 239, 50, 61);
		int LOGO_BACKGROUND_COLOR_DARK = ARGB32.color(255, 0, 0, 0);
		IntSupplier BRAND_BACKGROUND = () -> (Boolean)Minecraft.getInstance().options.darkMojangStudiosBackground().get() ? LOGO_BACKGROUND_COLOR_DARK : LOGO_BACKGROUND_COLOR;

		int alpha = Mth.ceil(logoAlpha * 255.0F);
		graphics.fill(RenderType.guiOverlay(), 0, 0, width, height, animated_logo$replaceAlpha(BRAND_BACKGROUND.getAsInt(), alpha));

		//? >= 1.21.2 {
		/*graphics.nextStratum();
		graphics.fill(0, 0, width, height, animated_logo$replaceAlpha(BRAND_BACKGROUND.getAsInt(), alpha));
		*///? }

		//? }

        if (!inited) {
            this.frames = new ResourceLocation[FRAMES];

            for (int i = 0; i < FRAMES; i++) {
                this.frames[i] = ResourceLocation.fromNamespaceAndPath(AnimatedLogo.MOD_ID, "textures/gui/frame_" + i + ".png");
            }

            if (!reload.isDone()) {
                final Minecraft client = Minecraft.getInstance();
                final ResourceManager resourceManager = client.getResourceManager();
                final ResourceLocation soundId = ResourceLocation.fromNamespaceAndPath(AnimatedLogo.MOD_ID, "logo.wav");

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
				//? >=1.21.2
                //RenderPipelines.MOJANG_LOGO,
				this.frames[count / IMAGE_PER_FRAME / FRAMES_PER_FRAME],
				contentX - logoWidthHalf, logoY - logoHeightHalf,
				//? 1.21
				(int) logoWidth, (int) logoHeight,
                0, 256 * ((count % (IMAGE_PER_FRAME * FRAMES_PER_FRAME)) / FRAMES_PER_FRAME),
				//? >=1.21.2
				//(int) logoWidth, (int) logoHeight,
				1024, 256,
				1024, 1024
				//? >=1.21.2
				//, ARGB32.white(logoAlpha)
        );

        if (progress >= 0.8) {
			//? >=1.21.2
            //f = Math.min(logoAlpha, f + 0.2f);

            int sw = (int) (logoWidth * 0.45);
            graphics.blit(
					//? >=1.21.2
					//RenderPipelines.MOJANG_LOGO,
                    ResourceLocation.fromNamespaceAndPath(AnimatedLogo.MOD_ID, "textures/gui/studios.png"),
					contentX - sw / 2, (int) (logoY - logoHeightHalf + logoHeight - logoHeight / 12),
					//? 1.21
					sw, (int) (logoHeight / 5.0),
                    0, 0,
					//? >=1.21.2
					//sw, (int) (logoHeight / 5.0),
					450, 50, 512, 512
					//? >=1.21.2
					//, ARGB32.white(f)
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

				//? fabric {
                /*this.fadeOutStart = -1L;
                this.fadeInStart = -1L;
				*///? }
            }
        }

		//? 1.21 && neoforge {
		ci.cancel();

		LoadingOverlayAccessor accessor = (LoadingOverlayAccessor) this;

		long millis = Util.getMillis();
		float fadeouttimer = this.fadeOutStart > -1L ? (float)(millis - this.fadeOutStart) / 1000.0F : -1.0F;
		if (fadeouttimer >= 2.0F) {
			this.progressMeter.complete();
			Minecraft.getInstance().setOverlay((Overlay)null);
			this.displayWindow.close();
		}

		if (this.fadeOutStart == -1L && this.reload.isDone()) {
			this.fadeOutStart = Util.getMillis();

			try {
				this.reload.checkExceptions();
				this.onFinish.accept(Optional.empty());
			} catch (Throwable throwable) {
				this.onFinish.accept(Optional.of(throwable));
			}

			if (Minecraft.getInstance().screen != null) {
				Minecraft.getInstance().screen.init(Minecraft.getInstance(), Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight());
			}
		}
		//? }
    }
}
