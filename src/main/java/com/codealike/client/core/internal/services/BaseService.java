package com.codealike.client.core.internal.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseService implements Runnable {
    private final ExecutorService ex  = Executors.newSingleThreadExecutor();

    private final List<ServiceListener> serviceListeners = new ArrayList<ServiceListener>();

    public void addListener(ServiceListener serviceListener) {
        serviceListeners.add(serviceListener);
    }

    // Use proper access modifier
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
