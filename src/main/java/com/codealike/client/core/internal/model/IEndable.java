package com.codealike.client.core.internal.model;

import com.codealike.client.core.internal.dto.ActivityType;
import org.joda.time.DateTime;
import org.joda.time.Period;

public interface IEndable {
    DateTime getCreationTime();
    Period getDuration();
    void setDuration(Period duration);
    ActivityType getType();
}
