/*
 * Copyright (c) 2022. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Version DTO class.
 *
 * @author Daniel, pvmagacho
 * @version 1.5.0.2
 */
public class Version {
    private int major;
    private int minor;
    private int build;
    private int revision;

    /**
     * Default constructor.
     */
    public Version() {
        this.major = -1;
        this.minor = -1;
        this.revision = -1;
        this.build = -1;
    }

    /**
     * Version constructor with major and minor versions.
     *
     * @param major the major version
     * @param minor the minor version
     */
    public Version(int major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    public int getMajor() {
        return major;
    }

    @JsonProperty("_Major")
    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    @JsonProperty("_Minor")
    public void setMinor(int minor) {
        this.minor = minor;
    }

    public int getBuild() {
        return build;
    }

    @JsonProperty("_Build")
    public void setBuild(int build) {
        this.build = build;
    }

    public int getRevision() {
        return revision;
    }

    @JsonProperty("_Revision")
    public void setRevision(int revision) {
        this.revision = revision;
    }

}
