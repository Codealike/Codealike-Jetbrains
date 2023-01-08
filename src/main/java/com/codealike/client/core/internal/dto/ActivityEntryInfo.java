package com.codealike.client.core.internal.dto;

import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.UUID;

public class ActivityEntryInfo {

    private UUID parentId;
    private DateTime start;
    private DateTime end;
    private ActivityType type;
    private Period duration;
    private CodeContextInfo context;

    public ActivityEntryInfo() {
    }

    public ActivityEntryInfo(UUID parentId) {
        this.parentId = parentId;
    }

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    public CodeContextInfo getContext() {
        return context;
    }

    public void setContext(CodeContextInfo context) {
        this.context = context;
    }

    public ActivityType getType() {
        return type;
    }

    public void setType(ActivityType type) {
        this.type = type;
    }

    public DateTime getStart() {
        return start;
    }

    public void setStart(DateTime start) {
        this.start = start;
    }

    public DateTime getEnd() {
        return end;
    }

    public void setEnd(DateTime end) {
        this.end = end;
    }

    public Period getDuration() {
        return duration;
    }

    public void setDuration(Period duration) {
        this.duration = duration;
    }

}
