package com.cyao.animatedlogo.mixin;

import net.minecraft.client.gui.screens.LoadingOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LoadingOverlay.class)
public interface LoadingOverlayAccessor {
	@Accessor("fadeOutStart")
	long fadeOutStart();
	@Accessor("fadeOutStart")
	void fadeOutStart(long i);

	@Accessor("fadeInStart")
	long fadeInStart();
}
