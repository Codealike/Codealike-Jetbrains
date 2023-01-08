package com.codealike.client.core.internal.model;

import com.codealike.client.core.internal.dto.ActivityType;

import java.util.UUID;

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
