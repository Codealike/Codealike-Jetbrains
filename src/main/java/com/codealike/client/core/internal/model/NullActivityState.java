/*
 * Copyright (c) 2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.model;

import java.util.UUID;

import org.joda.time.DateTime;

import com.codealike.client.core.internal.dto.ActivityType;
import com.codealike.client.core.internal.startup.PluginContext;

/**
 * Null activity state model.
 *
 * @author Daniel, pvmagacho
 * @version 1.5.0.2
 */
public class NullActivityState extends ActivityState {

    protected static NullActivityState createNew() {
        NullActivityState state = new NullActivityState(ActivityType.Idle, DateTime.now(), PluginContext.UNASSIGNED_PROJECT);

        return state;
    }

    protected static NullActivityState createNew(UUID projectId) {
        NullActivityState state = new NullActivityState(ActivityType.Idle, DateTime.now(), projectId);

        return state;
    }

    public NullActivityState(ActivityType type, DateTime creationTime, UUID projectId) {
        super(projectId, type, creationTime);
    }

    @Override
    public NullActivityState recreate() {
        return new NullActivityState(this.type, DateTime.now(), this.projectId);
    }

}
