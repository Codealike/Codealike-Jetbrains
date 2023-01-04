/*
 * Copyright (c) 2022-2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.dto;

import com.codealike.client.core.internal.utils.ExceptionUtils;

/**
 * Health information DTO class.
 *
 * @author Daniel, pvmagacho
 * @version 1.5.0.2
 */
public class HealthInfo {
    private String identity;
    private String source;
    private String message;
    private HealthInfoType type;

    /**
     * Health information class constructor.
     *
     * @param ex       the exception generated
     * @param message  the message
     * @param source   the source
     * @param type     the type
     * @param identity the user identity
     */
    public HealthInfo(Exception ex, String message, String source, HealthInfoType type, String identity) {
        this.identity = identity;
        this.message = message + " Details: " + ExceptionUtils.toString(ex);
        this.type = type;
        this.source = source;
    }

    public String getIdentity() {
        return identity;
    }

    public String getSource() {
        return source;
    }

    public String getMessage() {
        return message;
    }

    public HealthInfoType getType() {
        return type;
    }

    public enum HealthInfoType {
        Info,
        Warn,
        Error
    }
}
