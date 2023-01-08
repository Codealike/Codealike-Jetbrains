/*
 * Copyright (c) 2022-2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Base abstract class to push events.
 *
 * @author pvmagacho
 * @version 1.5.0.26
 */
public abstract class BaseService implements Runnable {
    // Single thread executor to run all tasks in a single queue
    private final ExecutorService ex = Executors.newSingleThreadExecutor();

    // Array with registered listeners
    private final List<ServiceListener> serviceListeners = new ArrayList<ServiceListener>();

    /**
     * Add a listener to the registered listener array for execution.
     *
     * @param serviceListener the listener to add. Must comply with {@link ServiceListener} interface.
     */
    public void addListener(ServiceListener serviceListener) {
        serviceListeners.add(serviceListener);
    }

    /**
     * Publish the event to the executor.
     */
    protected void publishEvent() {
        ex.submit(this);
    }

    @Override
    public void run() {
        System.out.println(this + " is going to publish to " + serviceListeners + " listeners.");
        for (ServiceListener serviceListener : serviceListeners) {
            serviceListener.onEvent();
        }
    }
}
