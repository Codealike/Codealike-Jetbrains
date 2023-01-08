package com.codealike.client.core.internal.dto;

import com.codealike.client.core.internal.serialization.ActivityTypeDeserializer;
import com.codealike.client.core.internal.serialization.ActivityTypeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = ActivityTypeSerializer.class)
@JsonDeserialize(using = ActivityTypeDeserializer.class)
public enum ActivityType {

    None(-1),
    Idle(0),
    System(1),
    Coding(2),
    Debugging(3),
    Navigating(4),
    Building(5),

    Event(1000),

    DocumentFocus(1001),
    DocumentEdit(1002),

    OpenSolution(1003),
    CloseSolution(1004),
    BuildSolutionFailed(1005),
    BuildSolutionSucceded(1006),
    BuildSolutionCancelled(1007),

    BuildProject(1008),
    BuildProjectFailed(1009),
    BuildProjectSucceeded(1010),
    BuildProjectCancelled(1011);

    private final int id;

    ActivityType(int id) {
        this.id = id;
    }

    public static ActivityType fromId(int id) {
        for (ActivityType value : ActivityType.values()) {
            if (value.getId() == id) {
                return value;
            }
        }
        throw new EnumConstantNotPresentException(ActivityType.class, "" + id);
    }

    public int getId() {
        return id;
    }
}