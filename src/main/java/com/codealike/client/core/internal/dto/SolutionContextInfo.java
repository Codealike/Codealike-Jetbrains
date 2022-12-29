/*
 * Copyright (c) 2022. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.dto;

import java.util.UUID;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Solution context information DTO class.
 *
 * @author Daniel, pvmagacho
 * @version 1.5.0.2
 */
public class SolutionContextInfo {

    @JsonProperty("SolutionId")
    private UUID solutionId;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("CreationTime")
    private DateTime creationTime;

    /**
     * Default constructor.
     */
    public SolutionContextInfo() {
        this.creationTime = new DateTime(0);
    }

    /**
     * Solution context information constructor
     *
     * @param solutionID the solution UUID
     * @param name       the context name
     */
    public SolutionContextInfo(UUID solutionID, String name) {
        this.solutionId = solutionID;
        this.name = name;
        this.creationTime = DateTime.now();
    }

    public UUID getSolutionId() {
        return solutionId;
    }

    @JsonProperty("SolutionId")
    public void setSolutionId(UUID SolutionId) {
        this.solutionId = SolutionId;
    }

    public String getName() {
        return name;
    }

    @JsonProperty("Name")
    public void setName(String name) {
        this.name = name;
    }

    public DateTime getCreationTime() {
        return creationTime;
    }

    @JsonProperty("CreationTime")
    public void setCreationTime(DateTime creationTime) {
        this.creationTime = creationTime;
    }
}
