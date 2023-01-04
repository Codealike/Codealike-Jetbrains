/*
 * Copyright (c) 2022-2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.dto;

import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.Period;

/**
 * Activity entry information DTO class.
 *
 * @author Daniel, pvmagacho
 * @version 1.5.0.2
 */
public class ActivityEntryInfo {

    private UUID parentId;
    private DateTime start;
    private DateTime end;
    private ActivityType type;
    private Period duration;
    private CodeContextInfo context;

    /**
     * Default constructor.
     */
    public ActivityEntryInfo() {
    }

    /**
     * Activity Entry constructor with parent UUID.
     *
     * @param parentId the parent UUID
     */
    public ActivityEntryInfo(UUID parentId) {
        this.parentId = parentId;
    }

    public void setStart(DateTime start) {
        this.start = start;
    }

    public void setEnd(DateTime end) {
        this.end = end;
    }

    public void setType(ActivityType type) {
        this.type = type;
    }

    public void setDuration(Period duration) {
        this.duration = duration;
    }

    public void setContext(CodeContextInfo context) {
        this.context = context;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    public UUID getParentId() {
        return parentId;
    }

    public CodeContextInfo getContext() {
        return context;
    }

    public ActivityType getType() {
        return type;
    }

    public DateTime getStart() {
        return start;
    }

    public DateTime getEnd() {
        return end;
    }

    public Period getDuration() {
        return duration;
    }

}
