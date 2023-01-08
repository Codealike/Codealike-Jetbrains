package com.codealike.client.core.internal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PluginSettingsInfo {
    @JsonProperty("idleCheckInterval")
    private int idleCheckInterval;
    @JsonProperty("idleMaxPeriod")
    private int idleMaxPeriod;
    @JsonProperty("flushInterval")
    private int flushInterval;

    public PluginSettingsInfo() {
        this.idleCheckInterval = 0;
        this.idleMaxPeriod = 0;
        this.flushInterval = 0;
    }

    public int getIdleCheckInterval() {
        return idleCheckInterval;
    }

    @JsonProperty("idleCheckInterval")
    public void setIdleCheckInterval(int idleCheckInterval) {
        this.idleCheckInterval = idleCheckInterval;
    }

    public int getIdleMaxPeriod() {
        return idleMaxPeriod;
    }

    @JsonProperty("idleMaxPeriod")
    public void setIdleMaxPeriod(int idleMaxPeriod) {
        this.idleMaxPeriod = idleMaxPeriod;
    }

    public int getFlushInterval() {
        return flushInterval;
    }

    @JsonProperty("flushInterval")
    public void setFlushInterval(int flushInterval) {
        this.flushInterval = flushInterval;
    }
}
