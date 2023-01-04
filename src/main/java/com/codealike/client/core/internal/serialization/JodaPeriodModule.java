/*
 * Copyright (c) 2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.serialization;

import org.joda.time.DateTime;
import org.joda.time.Period;

import com.fasterxml.jackson.databind.module.SimpleModule;

public class JodaPeriodModule extends SimpleModule {

	private static final long serialVersionUID = -8783171321786654936L;
	
	public JodaPeriodModule() {
		addSerializer(Period.class, new PeriodSerializer());
		addDeserializer(Period.class, new PeriodDeserializer());
		
		addSerializer(DateTime.class, new DateTimeSerializer());
		addDeserializer(DateTime.class, new DateTimeDeserializer());
	}
}
