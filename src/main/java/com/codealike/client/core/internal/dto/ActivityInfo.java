/*
 * Copyright (c) 2022. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.dto;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Activity information DTO class.
 *
 * @author Daniel, pvmagacho
 * @version 1.5.0.2
 */
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

    /**
     * Default constructor.
     */
    public ActivityInfo() {
    }

    /**
     * Activity info constructor.
     *
     * @param instance   the instance name
     * @param solutionId the UUID for the solution
     * @param batchId    the UUID for the batch
     */
    public ActivityInfo(String instance, UUID solutionId, UUID batchId) {
        this.instance = instance;
        this.solutionId = solutionId;
        this.batchId = batchId;
    }

    public void setMachine(String machine) {
        this.machine = machine;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public void setProjects(List<ProjectContextInfo> projectsInfo) {
        this.projects = projectsInfo;
    }

    public void setStates(List<ActivityEntryInfo> statesInfo) {
        this.states = statesInfo;
    }

    public void setEvents(List<ActivityEntryInfo> eventsInfo) {
        this.events = eventsInfo;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public void setSolutionId(UUID solutionId) {
        this.solutionId = solutionId;
    }

    public void setBatchId(UUID batchId) {
        this.batchId = batchId;
    }

    public String getInstance() {
        return instance;
    }

    public String getMachine() {
        return machine;
    }

    public UUID getSolutionId() {
        return solutionId;
    }

    public UUID getBatchId() {
        return batchId;
    }

    public List<ProjectContextInfo> getProjects() {
        return projects;
    }

    public List<ActivityEntryInfo> getStates() {
        return states;
    }

    public List<ActivityEntryInfo> getEvents() {
        return events;
    }

    public String getClient() {
        return client;
    }

    public String getExtension() {
        return extension;
    }

    // Check if activity is valid
    @JsonIgnore
    public boolean isValid() {
		/*This does not make sense in Eclipse since each solution has only 1 project :)
		if (this.projects == null || this.projects.isEmpty()) {
			return false;
		}*/
        if (this.states == null || this.states.isEmpty()) {
            return false;
        }
        return true;
    }

}
