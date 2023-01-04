package com.codealike.client.core.internal.services;

import com.codealike.client.core.internal.model.Profile;
import com.codealike.client.core.internal.model.TrackActivity;

/**
 * Created by Daniel on 11/16/2016.
 */
public interface IIdentityService {
    boolean isAuthenticated();

    boolean login(String identity, String token, boolean storeCredentials, boolean rememberMe);

    boolean tryLoginWithStoredCredentials();

    String getIdentity();

    String getToken();

    Profile getProfile();

    TrackActivity getTrackActivity();

    boolean isCredentialsStored();

    void logOff();
}
