/*
 * Copyright (c) 2022-2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.utils;

import com.intellij.openapi.diagnostic.Logger;

/**
 * Log manager class. This class will be used to log messages to console.
 *
 * @author Daniel, pvmagacho
 * @version 1.5.0.26
 */
@SuppressWarnings("restriction")
public class LogManager {
    public static final LogManager INSTANCE = new LogManager();
    private static final Logger logger = Logger.getInstance("Codealike");

    public LogManager() {
    }

    public void logError(String msg) {
        logger.error("CodealikeApplicationComponent: " + msg);
    }

    public void logError(Throwable t, String msg) {
        logger.error("CodealikeApplicationComponent: " + msg, t);
    }

    public void logWarn(String msg) {
        logger.warn("CodealikeApplicationComponent: " + msg);
    }

    public void logWarn(Throwable t, String msg) {
        logger.warn("CodealikeApplicationComponent: " + msg, t);
    }

    public void logInfo(String msg) {
        logger.info("CodealikeApplicationComponent: " + msg);
    }
}
