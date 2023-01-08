/*
 * Copyright (c) 2022-2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.intellij;

import com.codealike.client.core.internal.services.IdentityService;
import com.codealike.client.core.internal.services.TrackingService;
import com.codealike.client.core.internal.startup.PluginContext;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

/**
 * Plugin project service.
 *
 * @author pvmagacho
 * @version 1.5.0.26
 */
public class CodealikeProjectService implements Disposable {
    private final Project project;

    public CodealikeProjectService(Project project) {
        this.project = project;
    }

    static CodealikeProjectService getInstance(Project project) {
        return project.getService(CodealikeProjectService.class);
    }

    @Override
    public void dispose() {
        onProjectClosed();
    }

    void onProjectOpened() {
        // called when project is opened
        PluginContext pluginContext = PluginContext.getInstance();
        TrackingService trackingService = pluginContext.getTrackingService();
        IdentityService identityService = pluginContext.getIdentityService();
        if (identityService.isAuthenticated()) {
            switch (identityService.getTrackActivity()) {
                case Always: {
                    trackingService.enableTracking();
                    trackingService.startTracking(project, DateTime.now());
                    break;
                }
                case AskEveryTime:
                case Never:
                    Notification note = new Notification("CodealikeApplicationComponent.Notifications",
                            "Codealike",
                            "Codealike  is not tracking your projects",
                            NotificationType.INFORMATION);
                    Notifications.Bus.notify(note);
                    break;
            }
        } else {
            trackingService.disableTracking();
        }
    }

    void onProjectClosed() {
        TrackingService trackingService = TrackingService.getInstance();

        // called when project is being closed
        if (trackingService.isTracking()) {
            trackingService.stopTracking(project);

            // only disable tracking when last project gets closed
            if (trackingService.getTrackedProjects().values().isEmpty())
                trackingService.disableTracking();
        }
    }

    static class CodealikeProjectServiceActivity implements StartupActivity {
        @Override
        public void runActivity(@NotNull Project project) {
            CodealikeProjectService.getInstance(project).onProjectOpened();
        }
    }
}
