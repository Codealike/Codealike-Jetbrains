/*
 * Copyright (c) 2022-2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.utils;

import java.util.UUID;

import org.joda.time.format.PeriodFormatter;

import com.codealike.client.core.internal.model.ActivityEvent;
import com.codealike.client.core.internal.model.ActivityState;
import com.codealike.client.core.internal.serialization.PeriodSerializer;
import com.codealike.client.core.internal.startup.PluginContext;

/**
 * Tracking console class. Used to print track events messages to console.
 *
 * @author Daniel, pvmagacho
 * @version 1.5.0.26
 */
public class TrackingConsole {
    // Singleton instance
    private static TrackingConsole _instance;
    // The plugin context instance
    private PluginContext context;
    // Flag to enable messages to the console
    private boolean enabled;

    // private constructor
    private TrackingConsole(PluginContext context) {
        this.context = context;
        this.enabled = Boolean.parseBoolean(context.getProperty("tracking-console.enabled"));
    }

    /**
     * Get the singleton {@link TrackingConsole} instance. If it doesn't exist, one is created.
     *
     * @return the {@link TrackingConsole} instance
     */
    public static TrackingConsole getInstance() {
        if (_instance == null) {
            _instance = new TrackingConsole(PluginContext.getInstance());
        }

        return _instance;
    }

    /**
     * Track a generic message.
     *
     * @param the message to track
     */
    public void trackMessage(String message) {
        if (enabled) {
            System.out.println("---------------------------------------------------------------------");
            System.out.println(message);
            System.out.println("---------------------------------------------------------------------");
        }
    }

    /**
     * Track event record to console.
     *
     * @param event the {@link ActivityEvent} to track
     */
    public void trackEvent(ActivityEvent event) {
        if (enabled) {
            System.out.println("---------------------------------------------------------------------");
            String formattedDate = context.getDateTimeFormatter().print(event.getCreationTime());
            System.out.println(String.format("Event: type:%s, time:%s", event.getType().toString(), formattedDate));
            System.out.println(event.getContext().toString());
            System.out.println("---------------------------------------------------------------------");
        }
    }

    /**
     * Track state record to console.
     *
     * @param state the {@link ActivityState} to track
     */
    public void trackState(ActivityState state) {
        if (enabled) {
            PeriodFormatter formatter = PeriodSerializer.FORMATER;
            System.out.println(String.format("Last recorded state: type:%s, duration:%s\n", state.getType().toString(), state.getDuration().toString(formatter)));
        }
    }

    /**
     * Project tracking has ended.
     *
     * @param name the project name
     * @param id   the project UUID
     */
    public void trackProjectEnd(String name, UUID id) {
        if (enabled) {
            System.out.println(String.format("Stopped tracking project \"%s\" with id %s", name, id));
        }
    }

    /**
     * Project tracking has started.
     *
     * @param name the project name
     * @param id   the project UUID
     */
    public void trackProjectStart(String name, UUID id) {
        if (enabled) {
            System.out.println(String.format("Started tracking project \"%s\" with id %s", name, id));
        }
    }

}
