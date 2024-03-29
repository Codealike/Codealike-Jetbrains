/*
 * Copyright (c) 2022-2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.services;

import com.codealike.client.core.internal.model.TrackedProjectManager;
import com.codealike.client.core.internal.startup.PluginContext;
import com.codealike.client.core.internal.tracking.ActivitiesRecorder.FlushResult;
import com.codealike.client.core.internal.tracking.StateTracker;
import com.codealike.client.core.internal.utils.LogManager;
import com.codealike.client.core.internal.utils.TrackingConsole;
import com.google.common.collect.BiMap;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.joda.time.DateTime;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Tracking service class.
 *
 * @author Daniel, pvmagacho
 * @version 1.6.0.0
 */
public class TrackingService extends BaseService {
    private static TrackingService _instance;

    private TrackedProjectManager trackedProjectManager;
    private ScheduledExecutorService flushExecutor = null;
    private StateTracker tracker;
    private boolean isTracking;
    private PluginContext context;

    public TrackingService() {
        this.trackedProjectManager = new TrackedProjectManager();
        this.tracker = new StateTracker();
        this.isTracking = false;
    }

    public static TrackingService getInstance() {
        if (_instance == null) {
            _instance = new TrackingService();
        }
        if (_instance.context == null) {
            _instance.context = PluginContext.getInstance();
        }

        return _instance;
    }

    public void startTracking() {
        this.tracker.startTracking();

        startFlushExecutor();

        //We need to start tracking unassigned project for states like "debugging" which does not belong to any project.
        startTrackingUnassignedProject();

        this.isTracking = true;
        publishEvent();
    }

    public void trackDocumentFocus(Editor editor, int offset, int line) {
        tracker.trackDocumentFocus(editor, offset, line);
    }

    public void trackCodingEvent(Editor editor, int offset, int line) {
        tracker.trackCodingEvent(editor, offset, line);
    }

    private void startFlushExecutor() {
        if (this.flushExecutor != null)
            return;

        this.flushExecutor = Executors.newScheduledThreadPool(1);
        Runnable flushPeriodicTask = new Runnable() {

            @Override
            public void run() {
                try {
                    TrackingConsole.getInstance().trackMessage("Flush tracking information executed");
                    flushTrackingInformation();
                } catch (Exception e) {
                    TrackingConsole.getInstance().trackMessage("Flush tracking information error " + e.getMessage());
                }
            }
        };

        int flushInterval = this.context.getConfiguration().getFlushInterval();
        this.flushExecutor.scheduleAtFixedRate(flushPeriodicTask, flushInterval, flushInterval, TimeUnit.MILLISECONDS);
    }

    private void flushTrackingInformation() {
        Boolean verboseMode = Boolean.parseBoolean(context.getProperty("activity-verbose-notifications"));

        Notification resultNote = null;

        Notification note = new Notification("CodealikeApplicationComponent.Notifications",
                "Codealike",
                "Codealike is sending activities...",
                NotificationType.INFORMATION);
        if (verboseMode) {
            Notifications.Bus.notify(note);
        }

        FlushResult result = tracker.flush(context.getIdentityService().getIdentity(), context.getIdentityService().getToken());
        switch (result) {
            case Succeded:
                resultNote = new Notification("CodealikeApplicationComponent.Notifications",
                        "Codealike",
                        "Codealike sent activities",
                        NotificationType.INFORMATION);

                Notifications.Bus.notify(resultNote);

                break;
            case Skip:
                resultNote = new Notification("CodealikeApplicationComponent.Notifications",
                        "Codealike",
                        "No data to be sent",
                        NotificationType.INFORMATION);

                if (verboseMode) {
                    Notifications.Bus.notify(resultNote);
                }

                break;
            case Offline:
                resultNote = new Notification("CodealikeApplicationComponent.Notifications",
                        "Codealike",
                        "Codealike is working in offline mode",
                        NotificationType.INFORMATION);

                Notifications.Bus.notify(resultNote);

                break;
            case Report:
                resultNote = new Notification("CodealikeApplicationComponent.Notifications",
                        "Codealike",
                        "Codealike is storing corrupted entries for further inspection",
                        NotificationType.INFORMATION);

                if (verboseMode) {
                    Notifications.Bus.notify(resultNote);
                }
        }
    }

    public void stopTracking(boolean propagate) {
        this.tracker.stopTracking();
        if (this.flushExecutor != null) {
            this.flushExecutor.shutdownNow();
            this.flushExecutor = null;
        }

        this.trackedProjectManager.stopTracking();

        this.isTracking = false;
        if (propagate) {
            publishEvent();
        }
    }

    public void enableTracking() {
        if (context.isAuthenticated()) {
            startTracking();

            Notification note = new Notification("CodealikeApplicationComponent.Notifications",
                    "Codealike",
                    "Codealike is connected and tracking your projects.",
                    NotificationType.INFORMATION);
            Notifications.Bus.notify(note);
        }
    }

    public void disableTracking() {
        if (context.isAuthenticated()) {
            stopTracking(true);

            // flush last information before leaving
            flushTrackingInformation();

            Notification note = new Notification("CodealikeApplicationComponent.Notifications",
                    "Codealike",
                    "Codealike  is not tracking your projects",
                    NotificationType.INFORMATION);
            Notifications.Bus.notify(note);
        }
    }

    public synchronized void startTracking(Project project, DateTime workspaceInitDate) {
        if (!project.isOpen()) {
            return;
        }
        if (isTracked(project)) {
            return;
        }
        UUID projectId = PluginContext.getInstance().getOrCreateUUID(project);
        if (projectId != null && trackedProjectManager.trackProject(project, projectId)) {
            tracker.startTrackingProject(project, projectId, workspaceInitDate);
        } else {
            LogManager.INSTANCE.logWarn(String.format("Could not track project %s. "
                    + "If you have a duplicated UUID in any of your \"com.codealike.client.intellij.prefs\" please delete one of those to generate a new UUID for"
                    + "that project", project.getName()));
        }
    }

    private void startTrackingUnassignedProject() {
        try {
            PluginContext.getInstance().registerProjectContext(PluginContext.UNASSIGNED_PROJECT, "Unassigned");
        } catch (Exception e) {
            LogManager.INSTANCE.logWarn("Could not track unassigned project.");
        }
    }

    public boolean isTracked(Project project) {
        return this.trackedProjectManager.isTracked(project);
    }

    public void stopTracking(Project project) {
        this.trackedProjectManager.stopTrackingProject(project);
    }

    public TrackedProjectManager getTrackedProjectManager() {
        return this.trackedProjectManager;
    }

    public UUID getUUID(Project project) {
        return this.trackedProjectManager.getTrackedProjectId(project);
    }

    public Project getProject(UUID projectId) {
        return this.trackedProjectManager.getTrackedProject(projectId);
    }

    public BiMap<Project, UUID> getTrackedProjects() {
        return this.trackedProjectManager.getTrackedProjects();
    }

    public boolean isTracking() {
        return this.isTracking;
    }

    public void flushRecorder(final String identity, final String token) {
        if (this.isTracking) {
            this.flushExecutor.execute(new Runnable() {

                @Override
                public void run() {
                    tracker.flush(identity, token);
                }
            });
        }
    }

}
