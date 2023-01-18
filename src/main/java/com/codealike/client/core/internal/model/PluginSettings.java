/*
 * Copyright (c) 2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.model;

/**
 * Plugin settings model.
 *
 * @author Daniel, pvmagacho
 * @version 1.6.0.0
 */
public class PluginSettings {
    private int idleCheckInterval;
    private int idleMaxPeriod;
    private int flushInterval;

    public PluginSettings() {
        // set the default values
        this.idleCheckInterval = 30000;
        this.idleMaxPeriod = 60000;
        this.flushInterval = 300000;
    }

    public int getIdleCheckInterval() {
        return idleCheckInterval;
    }

    public void setIdleCheckInterval(int idleCheckInterval) {
        this.idleCheckInterval = idleCheckInterval;
    }

    public int getIdleMaxPeriod() {
        return idleMaxPeriod;
    }

    public void setIdleMaxPeriod(int idleMaxPeriod) {
        this.idleMaxPeriod = idleMaxPeriod;
    }

    public int getFlushInterval() {
        return flushInterval;
    }

    public void setFlushInterval(int flushInterval) {
        this.flushInterval = flushInterval;
    }
}
