/*
 * Copyright (c) 2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.serialization;

import com.codealike.client.core.internal.dto.ActivityType;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class ActivityTypeSerializer extends JsonSerializer<ActivityType> {

    @Override
    public void serialize(ActivityType type, JsonGenerator jgen, SerializerProvider arg2) throws IOException,
            JsonProcessingException {
        jgen.writeNumber(type.getId());
    }

}
