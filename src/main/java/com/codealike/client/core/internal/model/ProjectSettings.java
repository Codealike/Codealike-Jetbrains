/*
 * Copyright (c) 2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.model;

import java.util.UUID;

/**
 * Project settings model.
 *
 * @author Daniel, pvmagacho
 * @version 1.6.0.0
 */
public class ProjectSettings {
    private UUID projectId;
    private String projectName;

    public ProjectSettings() {
        this.setProjectName(null);
        this.setProjectId(null);
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
