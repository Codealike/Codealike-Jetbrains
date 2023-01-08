package com.codealike.client.core.internal.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;

public class ActivityInfo {

    private String machine;
    private String client;
    private String extension;
    private List<ProjectContextInfo> projects;
    private List<ActivityEntryInfo> states;
    private List<ActivityEntryInfo> events;
    private String instance;
    private UUID solutionId;
    private UUID batchId;
    private DateTime batchStart;
    private DateTime batchEnd;

    public ActivityInfo() {
    }

    public ActivityInfo(String instance, UUID solutionId, UUID batchId, DateTime batchStart, DateTime batchEnd) {
        this.instance = instance;
        this.solutionId = solutionId;
        this.batchId = batchId;
        this.batchStart = batchStart;
        this.batchEnd = batchEnd;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getMachine() {
        return machine;
    }

    public void setMachine(String machine) {
        this.machine = machine;
    }

    public UUID getSolutionId() {
        return solutionId;
    }

    public void setSolutionId(UUID solutionId) {
        this.solutionId = solutionId;
    }

    public UUID getBatchId() {
        return batchId;
    }

    public void setBatchId(UUID batchId) {
        this.batchId = batchId;
    }

    public List<ProjectContextInfo> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectContextInfo> projectsInfo) {
        this.projects = projectsInfo;
    }

    public List<ActivityEntryInfo> getStates() {
        return states;
    }

    public void setStates(List<ActivityEntryInfo> statesInfo) {
        this.states = statesInfo;
    }

    public List<ActivityEntryInfo> getEvents() {
        return events;
    }

    public void setEvents(List<ActivityEntryInfo> eventsInfo) {
        this.events = eventsInfo;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    @JsonIgnore
    public boolean isValid() {
        //This does not make sense in Eclipse since each solution has only 1 project :)
//		if (this.projects == null || this.projects.isEmpty()) {
//			return false;
//		}
        if (this.states == null || this.states.isEmpty()) {
            return false;
        }
        return true;
    }

    public DateTime getBatchStart() {
        return batchStart;
    }

    public void setBatchStart(DateTime batchStart) {
        this.batchStart = batchStart;
    }

    public DateTime getBatchEnd() {
        return batchEnd;
    }

    public void setBatchEnd(DateTime batchEnd) {
        this.batchEnd = batchEnd;
    }
}
