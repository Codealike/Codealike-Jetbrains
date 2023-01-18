package com.codealike.client.core.internal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;

import java.util.UUID;

public class SolutionContextInfo {

    @JsonProperty("SolutionId")
    private UUID solutionId;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("CreationTime")
    private DateTime creationTime;

    public SolutionContextInfo() {
        this.creationTime = new DateTime(0);
    }

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
