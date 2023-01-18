package com.codealike.client.core.internal.dto;

import com.codealike.client.core.internal.utils.ExceptionUtils;

public class HealthInfo {

    private String identity;
    private String source;
    private String message;
    private HealthInfoType type;

    public HealthInfo(Exception ex, String message, String source, HealthInfoType type, String identity) {
        this.identity = identity;
        this.message = message + " Details: " + ExceptionUtils.toString(ex);
        this.type = type;
        this.source = source;
    }

    public HealthInfo(String message, String source, HealthInfoType type, String identity) {
        this.identity = identity;
        this.message = message;
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
