package com.codealike.client.core.internal.utils;

import com.intellij.openapi.diagnostic.Logger;

@SuppressWarnings("restriction")
public class LogManager {
	private static final Logger logger = Logger.getInstance("Codealike");
	public static final LogManager INSTANCE = new LogManager();

    public LogManager() {
    }

	public void logError(String msg) {
        logger.error("CodealikeApplicationComponent: "+msg);
    }

	public void logError(Throwable t, String msg) {
        logger.error("CodealikeApplicationComponent: "+msg, t);
    }

	public void logWarn(String msg) {
		logger.warn("CodealikeApplicationComponent: "+msg);
	}

	public void logWarn(Throwable t, String msg) {
		logger.warn("CodealikeApplicationComponent: "+msg, t);
	}

	public void logInfo(String msg) {
		logger.info("CodealikeApplicationComponent: "+msg);
	}
}
