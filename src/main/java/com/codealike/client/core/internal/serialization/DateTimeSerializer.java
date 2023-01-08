package com.codealike.client.core.internal.serialization;

import com.codealike.client.core.internal.startup.PluginContext;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.joda.time.DateTime;

import java.io.IOException;

public class DateTimeSerializer extends JsonSerializer<DateTime> {


    @Override
    public void serialize(DateTime dateTime, JsonGenerator jgen, SerializerProvider provider) throws IOException,
            JsonProcessingException {
//		jgen.writeString(String.format("/Date(%d)/", dateTime.getMillis()));
        String formattedDate = PluginContext.getInstance().getDateTimeFormatter().print(dateTime);
        jgen.writeString(formattedDate);
    }

}
