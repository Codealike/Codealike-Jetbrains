package com.codealike.client.core.internal.model;

import com.codealike.client.core.internal.dto.ActivityType;
import com.codealike.client.core.internal.startup.PluginContext;
import org.joda.time.DateTime;

import java.util.UUID;

public class NullActivityState extends ActivityState {

    public NullActivityState(ActivityType type, DateTime creationTime, UUID projectId) {
        super(projectId, type, creationTime);
    }

    protected static NullActivityState createNew() {
        NullActivityState state = new NullActivityState(ActivityType.Idle, DateTime.now(), PluginContext.UNASSIGNED_PROJECT);

        return state;
    }

    protected static NullActivityState createNew(UUID projectId) {
        NullActivityState state = new NullActivityState(ActivityType.Idle, DateTime.now(), projectId);

        return state;
    }

    @Override
    public NullActivityState recreate() {
        return new NullActivityState(this.type, DateTime.now(), this.projectId);
    }

}
