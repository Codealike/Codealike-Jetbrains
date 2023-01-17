/*
 * Copyright (c) 2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.model;

/**
 * Profile model.
 *
 * @author Daniel, pvmagacho
 * @version 1.5.0.26
 */
public class Profile {

    private String identity;
    private String fullName;
    private String displayName;
    private String address;
    private String state;
    private String country;
    private String avatarUri;
    private String email;

    public Profile(String identity, String fullName, String displayName, String address, String state, String country, String avatarUri, String email) {
        this.identity = identity;
        this.fullName = fullName;
        this.displayName = displayName;
        this.address = address;
        this.state = state;
        this.country = country;
        this.avatarUri = avatarUri;
        this.email = email;
    }

    public String getIdentity() {
        return identity;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAddress() {
        return address;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public String getAvatarUri() {
        return avatarUri;
    }

    public String getEmail() {
        return email;
    }

}
