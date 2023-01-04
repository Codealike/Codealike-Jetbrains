/*
 * Copyright (c) 2022-2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Profile information DTO class
 *
 * @author Daniel, pvmagacho
 * @version 1.5.0.2
 */
public class ProfileInfo {

    private String identity;
    private String fullName;
    private String displayName;
    private String address;
    private String state;
    private String country;
    private String avatarUri;
    private String email;

    public String getIdentity() {
        return identity;
    }

    @JsonProperty("Identity")
    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getFullName() {
        return fullName;
    }

    @JsonProperty("FullName")
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @JsonProperty("DisplayName")
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAddress() {
        return address;
    }

    @JsonProperty("Address")
    public void setAddress(String address) {
        this.address = address;
    }

    public String getState() {
        return state;
    }

    @JsonProperty("State")
    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    @JsonProperty("Country")
    public void setCountry(String country) {
        this.country = country;
    }

    public String getAvatarUri() {
        return avatarUri;
    }

    @JsonProperty("AvatarUri")
    public void setAvatarUri(String avatarUri) {
        this.avatarUri = avatarUri;
    }

    public String getEmail() {
        return email;
    }

    @JsonProperty("Email")
    public void setEmail(String email) {
        this.email = email;
    }
}
