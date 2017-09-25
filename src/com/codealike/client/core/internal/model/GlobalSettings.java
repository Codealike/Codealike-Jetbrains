package com.codealike.client.core.internal.model;

public class GlobalSettings {
    private String userToken;
    private String apiUrl;
    private long idleCheckInterval;
    private long idleMaxPeriod;
    private long flushInterval;
    private boolean trackSent;

    public GlobalSettings() {
        this.setIdleCheckInterval(30000); // in milliseconds
        this.setIdleMaxPeriod(60000); // in milliseconds
        this.setFlushInterval(300000); // in milliseconds
        this.trackSent = true;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public long getIdleCheckInterval() {
        return idleCheckInterval;
    }

    public void setIdleCheckInterval(long idleCheckInterval) {
        this.idleCheckInterval = idleCheckInterval;
    }

    public long getIdleMaxPeriod() {
        return idleMaxPeriod;
    }

    public void setIdleMaxPeriod(long idleMaxPeriod) {
        this.idleMaxPeriod = idleMaxPeriod;
    }

    public long getFlushInterval() {
        return flushInterval;
    }

    public void setFlushInterval(long flushInterval) {
        this.flushInterval = flushInterval;
    }

    public boolean getTrackSent() {
        return trackSent;
    }

    public void setTrackSent(boolean trackSent) {
        this.trackSent = trackSent;
    }
}
