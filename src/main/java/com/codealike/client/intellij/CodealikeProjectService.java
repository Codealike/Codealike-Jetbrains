/*
 * Copyright (c) 2022-2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.intellij;

import com.codealike.client.core.internal.services.TrackingService;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

/**
 * Plugin project service.
 *
 * @author pvmagacho
 * @version 1.5.0.2
 */
public class CodealikeProjectService implements Disposable {
    private final Project project;

    public CodealikeProjectService(Project project) {
        this.project = project;
    }

    @Override
    public void dispose() {
        onProjectClosed();
    }

    void onProjectOpened() {
        // called when project is opened
        if (TrackingService.getInstance().isTracking()) {
            TrackingService.getInstance().startTracking(project);
        }
    }

    void onProjectClosed() {
        // called when project is being closed
        if (TrackingService.getInstance().isTracking()) {
            TrackingService.getInstance().stopTracking(project);
        }
    }

    static CodealikeProjectService getInstance(Project project) {
        return project.getService(CodealikeProjectService.class);
    }

    static class CodealikeProjectServiceActivity implements StartupActivity {
        @Override
        public void runActivity(@NotNull Project project) {
            CodealikeProjectService.getInstance(project).onProjectOpened();
        }
    }
}
