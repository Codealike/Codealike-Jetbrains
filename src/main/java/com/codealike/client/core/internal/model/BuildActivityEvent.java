/*
 * Copyright (c) 2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.model;

import java.util.UUID;

import com.codealike.client.core.internal.dto.ActivityType;

/**
 * Build activity event model.
 *
 * @author Daniel, pvmagacho
 * @version 1.5.0.2
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
