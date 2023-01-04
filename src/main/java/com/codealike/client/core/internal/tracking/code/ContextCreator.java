/*
 * Copyright (c) 2022-2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.tracking.code;

import java.util.UUID;

import com.codealike.client.core.internal.model.CodeContext;
import com.codealike.client.core.internal.model.StructuralCodeContext;
import com.codealike.client.core.internal.startup.PluginContext;
import com.intellij.openapi.project.Project;

/**
 * Context creator class.
 *
 * @author Daniel, pvmagacho
 * @version 1.5.0.2
 */
public class ContextCreator {

    /**
     * Create the code context model from a {@link Project}.
     *
     * @param project the current project
     * @return the created code context model
     */
    public CodeContext createCodeContext(Project project) {
        UUID projectId = PluginContext.getInstance().getTrackingService().getUUID(project);

        CodeContext context = new StructuralCodeContext(projectId);
        context.setProject(project.getName());

        return context;
    }

}
