package com.codealike.client.core.internal.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtils {


	public static String toString(Throwable t) { 
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		
		t.printStackTrace(pw);
		pw.flush();
		
		return sw.toString();
	}
}
