/*
 * Copyright (c) 2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.model;

import com.codealike.client.core.internal.dto.ActivityType;

import java.util.UUID;

/**
 * Build activity event model.
 *
 * @author Daniel, pvmagacho
 * @version 1.6.0.0
 */
public class BuildActivityEvent extends ActivityEvent {

    public BuildActivityEvent(UUID projectId, ActivityType type, CodeContext context) {
        super(projectId, type, context);
    }

    @Override
    public boolean isMarker() {
        return this.type != ActivityType.BuildProject;
    }

    @Override
    public boolean isBuildEvent() {
        return true;
    }

}
