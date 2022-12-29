/*
 * Copyright (c) 2022. All rights reserved to Torc LLC.
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
 * @version 1.5.0.2
 */
public class TrackingConsole {

    private static TrackingConsole _instance;
    private PluginContext context;
    private boolean enabled;

    public static TrackingConsole getInstance() {
        if (_instance == null) {
            _instance = new TrackingConsole(PluginContext.getInstance());
        }

        return _instance;
    }

    private TrackingConsole(PluginContext context) {
        this.context = context;
        this.enabled = Boolean.parseBoolean(context.getProperty("tracking-console.enabled"));
    }

    public void trackEvent(ActivityEvent event) {
        if (enabled) {
            System.out.println("---------------------------------------------------------------------");
            String formattedDate = context.getDateTimeFormatter().print(event.getCreationTime());
            System.out.println(String.format("Event: type:%s, time:%s", event.getType().toString(), formattedDate));
            System.out.println(event.getContext().toString());
            System.out.println("---------------------------------------------------------------------");
        }
    }

    public void trackState(ActivityState state) {
        if (enabled) {
            PeriodFormatter formatter = PeriodSerializer.FORMATER;
            System.out.println(String.format("Last recorded state: type:%s, duration:%s\n", state.getType().toString(), state.getDuration().toString(formatter)));
        }
    }

    public void trackProjectEnd(String name, UUID id) {
        if (enabled) {
            System.out.println(String.format("Stopped tracking project \"%s\" with id %s", name, id));
        }
    }

    public void trackProjectStart(String name, UUID id) {
        if (enabled) {
            System.out.println(String.format("Started tracking project \"%s\" with id %s", name, id));
        }
    }

}
