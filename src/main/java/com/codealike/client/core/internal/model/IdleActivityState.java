/*
 * Copyright (c) 2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.model;

import com.codealike.client.core.internal.dto.ActivityType;
import org.joda.time.DateTime;

import java.util.UUID;

/**
 * Idle activity state model.
 *
 * @author Daniel, pvmagacho
 * @version 1.5.0.26
 */
public class IdleActivityState extends ActivityState {

    private DateTime lastActivity;

    public IdleActivityState(UUID projectId, ActivityType type, DateTime creationTime) {
        super(projectId, type, creationTime);
    }

    protected static IdleActivityState createNew(UUID projectId) {
        IdleActivityState state = new IdleActivityState(projectId, ActivityType.Idle, DateTime.now());
        state.lastActivity = state.getCreationTime();

        return state;
    }

    public DateTime getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(DateTime lastActivity) {
        this.lastActivity = lastActivity;
    }

    @Override
    public IdleActivityState recreate() {
        return new IdleActivityState(this.projectId, this.type, DateTime.now());
    }

}
