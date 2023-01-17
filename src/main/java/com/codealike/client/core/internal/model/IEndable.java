/*
 * Copyright (c) 2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.model;

import com.codealike.client.core.internal.dto.ActivityType;
import org.joda.time.DateTime;
import org.joda.time.Period;

/**
 * Endable model interface.
 *
 * @author Daniel, pvmagacho
 * @version 1.5.0.26
 */
public interface IEndable {
    DateTime getCreationTime();

    Period getDuration();

    void setDuration(Period duration);

    ActivityType getType();
}
