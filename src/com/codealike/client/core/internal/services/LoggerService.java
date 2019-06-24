package com.codealike.client.core.internal.services;

import com.codealike.client.core.internal.utils.Configuration;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerService {
    private int logLevel = 0;
    private Logger logger = null;

    public LoggerService(Configuration configuration) {
        this.logLevel = configuration.getLogLevel();

        if (this.logLevel > 0) {
            FileHandler handler = null;
            try {
                System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %4$s %5$s%6$s%n");
                handler = new FileHandler(configuration.getLogFile().getPath());
                handler.setFormatter(new SimpleFormatter());
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.logger = Logger.getLogger("Codealike");
            if (handler != null) {
                this.logger.addHandler(handler);
            }
        }
    }

    public void log(String message) {
        if (this.logger != null)
            this.logger.info(message);
    }

    public void logInfo(String message) {
        if (this.logger != null)
            this.logger.info(message);
    }

    public void logError(Throwable error, String message) {
        if (this.logger != null)
            this.logger.log(Level.SEVERE, message + " " + error.getMessage());
    }

    public void logWarn(String message) {
        if (this.logger != null)
            this.logger.log(Level.WARNING, message);
    }

    public void logError(String message) {
        if (this.logger != null)
            this.logger.log(Level.SEVERE, message);
    }
}
