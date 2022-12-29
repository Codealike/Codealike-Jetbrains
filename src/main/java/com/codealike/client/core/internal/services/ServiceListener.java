/*
 * Copyright (c) 2022. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.services;

/**
 * Service listener interface.
 *
 * @author pvmagacho
 * @version 1.5.0.2
 */
public interface ServiceListener {
    /**
     * Method to be executed by the implementation class.
     */
    void onEvent();
}