package com.codealike.client.core.internal.serialization;

import java.io.IOException;

import com.codealike.client.core.internal.dto.ActivityType;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ActivityTypeSerializer extends JsonSerializer<ActivityType> {

	@Override
	public void serialize(ActivityType type, JsonGenerator jgen, SerializerProvider arg2) throws IOException,
			JsonProcessingException {
		jgen.writeNumber(type.getId());
	}

}
