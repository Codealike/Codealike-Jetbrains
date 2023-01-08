package com.codealike.client.core.internal.dto;

import java.util.UUID;

public class ProjectContextInfo {

    private UUID projectId;
    private String name;

    public ProjectContextInfo() {
    }

    public ProjectContextInfo(UUID projectId, String project) {
        this.projectId = projectId;
        this.name = project;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
