/*
 * Copyright (c) 2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.serialization;

import java.io.IOException;

import com.codealike.client.core.internal.dto.ActivityType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class ActivityTypeDeserializer extends JsonDeserializer<ActivityType> {

	@Override
	public ActivityType deserialize(JsonParser jsonParser, DeserializationContext context)
			throws IOException, JsonProcessingException {
		if (jsonParser.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) {
			return ActivityType.fromId(jsonParser.getNumberValue().intValue());
		}
		// throw context.mappingException("Expected int value to parse an ActivityType");
		throw context.instantiationException(ActivityType.class,"Expected int value to parse an ActivityType");
	}

}
