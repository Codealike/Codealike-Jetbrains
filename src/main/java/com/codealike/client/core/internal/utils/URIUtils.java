/*
 * Copyright (c) 2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;

public class URIUtils {
	
	public static Map<String, String> convertQueryParameters(List<NameValuePair> parameters) {
		Map<String, String> parametersMap = new HashMap<String, String>();
		
		for (NameValuePair parameter : parameters) {
			parametersMap.put(parameter.getName().toLowerCase(), parameter.getValue());
		}
		
		return parametersMap;
	}

}
