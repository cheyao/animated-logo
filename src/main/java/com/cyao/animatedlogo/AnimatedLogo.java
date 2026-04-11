package com.cyao.animatedlogo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("LoggingSimilarMessage")
public class AnimatedLogo {
	public static final String MOD_ID = /*$ mod_id*/ "animatedlogo";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static void onInitialize() {
		LOGGER.info("Initializing mod");
	}
}
