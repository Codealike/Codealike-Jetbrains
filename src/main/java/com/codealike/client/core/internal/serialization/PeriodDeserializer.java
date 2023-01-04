/*
 * Copyright (c) 2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.serialization;

import java.io.IOException;

import org.joda.time.Period;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class PeriodDeserializer extends JsonDeserializer<Period> {

	
	public PeriodDeserializer() {
	}
	
	@Override
	public Period deserialize(JsonParser jsonParser, DeserializationContext context)
			throws IOException, JsonProcessingException {
		if (jsonParser.getCurrentToken() == JsonToken.VALUE_STRING) {
			Period period = Period.parse(jsonParser.getValueAsString(), PeriodSerializer.FORMATER);
			if (period != null)
			{
				return period;
			}
		}

		throw context.instantiationException(Period.class, "Expected string");
	}

}
