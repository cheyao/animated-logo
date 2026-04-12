package com.cyao.animatedlogo.platform.fabric;

//? fabric {
import com.cyao.animatedlogo.AnimatedLogo;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import net.fabricmc.api.ModInitializer;

@Entrypoint("main")
public class FabricEntrypoint implements ModInitializer {

	@Override
	public void onInitialize() {
		AnimatedLogo.onInitialize();
	}
}
//?}
