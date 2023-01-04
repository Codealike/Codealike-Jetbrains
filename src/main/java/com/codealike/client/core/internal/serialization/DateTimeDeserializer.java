/*
 * Copyright (c) 2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.serialization;

import java.io.IOException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import com.codealike.client.core.internal.startup.PluginContext;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class DateTimeDeserializer extends JsonDeserializer<DateTime> {

	@Override
	public DateTime deserialize(JsonParser jsonParser, DeserializationContext context)
			throws IOException, JsonProcessingException {
		
		DateTimeFormatter formatter = PluginContext.getInstance().getDateTimeParser();

		if (jsonParser.getCurrentToken() == JsonToken.VALUE_STRING ) {
				String date = jsonParser.getValueAsString();
				String[] tokens = date.split("\\.");
				if (tokens.length >1 && tokens[1].length() > 3) {
					String fractionalSecsAsString = tokens[1].replace("Z", "");
					int fractionalSecs = Integer.parseInt(fractionalSecsAsString) / 10000;
					return formatter.parseDateTime(String.format("%s.%03d", tokens[0], fractionalSecs));
				}
				return formatter.parseDateTime(date);
		}
		
		throw context.instantiationException(DateTime.class, "Expected string value to parse a DateTime");
	}

}
