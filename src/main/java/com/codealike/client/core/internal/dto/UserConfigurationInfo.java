/*
 * Copyright (c) 2022. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.dto;

import com.codealike.client.core.internal.model.TrackActivity;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User configuration information DTO class.
 *
 * @author Daniel, pvmagacho
 * @version 1.5.0.2
 */
public class UserConfigurationInfo {

    private TrackActivity trackActivities;

    public TrackActivity getTrackActivities() {
        return trackActivities;
    }

    @JsonProperty("TrackActivities")
    public void setTrackActivities(TrackActivity trackActivities) {
        this.trackActivities = trackActivities;
    }

}
