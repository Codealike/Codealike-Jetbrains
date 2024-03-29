/*
 * Copyright (c) 2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.model;

import com.codealike.client.core.internal.dto.ActivityType;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.UUID;

/**
 * Activity event model.
 *
 * @author Daniel, pvmagacho
 * @version 1.6.0.0
 */
public class ActivityEvent implements IEndable {

    protected ActivityType type;
    protected CodeContext context;
    protected DateTime creationTime;
    protected Period duration;
    protected UUID projectId;


    public ActivityEvent(UUID projectId, ActivityType type, CodeContext context) {
        creationTime = DateTime.now();
        duration = Period.ZERO;
        this.type = type;
        this.context = context;
        this.projectId = projectId;
    }

    public ActivityType getType() {
        return type;
    }

    public CodeContext getContext() {
        return context;
    }

    public void setContext(CodeContext context) {
        this.context = context;
    }

    public DateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(DateTime creationTime) {
        this.creationTime = creationTime;
    }

    public Period getDuration() {
        return duration;
    }

    public void setDuration(Period duration) {
        this.duration = duration;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public boolean isMarker() {
        return this.type == ActivityType.Event;
    }

    public boolean canSpan() {
        return this.type == ActivityType.DocumentEdit || this.type == ActivityType.DocumentFocus;
    }


    public boolean isBuildEvent() {
        return false;
    }

    public ActivityEvent recreate() {
        return new ActivityEvent(this.projectId, this.type, this.getContext());
    }

    public void closeDuration(DateTime closeTo) {
        this.duration = new Period(this.getCreationTime(), closeTo);
    }

    public boolean isEquivalent(ActivityEvent event) {
        if (event == null) return false;
        return (this.getType() == event.getType()
                && this.getContext().isEquivalent(event.getContext()));
    }

    @Override
    public boolean equals(Object event) {
        if (event == null) return false;
        if (event == this) return true;
        if (!(event instanceof ActivityEvent)) return false;
        ActivityEvent eventClass = (ActivityEvent) event;

        return (this.getProjectId() == eventClass.getProjectId()
                && this.getType() == eventClass.getType()
                && this.getContext().equals(eventClass.getContext()));
    }
}
