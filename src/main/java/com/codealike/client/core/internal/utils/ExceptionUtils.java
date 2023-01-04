/*
 * Copyright (c) 2022-2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Exception utils class.
 *
 * @author Daniel, pvmagacho
 * @version 1.5.0.2
 */
public class ExceptionUtils {

    /**
     * Convert an exception stack trace to a string message.
     *
     * @param t the throwable to convert
     * @return the stack trace string
     */
    public static String toString(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);

        t.printStackTrace(pw);
        pw.flush();

        return sw.toString();
    }
}
