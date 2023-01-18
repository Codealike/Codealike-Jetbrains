/*
 * Copyright (c) 2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Global settings event model.
 *
 * @author Daniel, pvmagacho
 * @version 1.6.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GlobalSettings {
    private String userToken;
    private String apiUrl;
    private boolean trackSent;

    public GlobalSettings() {
        this.setApiUrl("https://codealike.com/api/v2");
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

    public boolean getTrackSent() {
        return trackSent;
    }

    public void setTrackSent(boolean trackSent) {
        this.trackSent = trackSent;
    }
}
