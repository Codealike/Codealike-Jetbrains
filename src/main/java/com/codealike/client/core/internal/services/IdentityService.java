package com.codealike.client.core.internal.services;

import java.security.KeyManagementException;
import java.util.Observable;

import com.codealike.client.core.api.ApiClient;
import com.codealike.client.core.api.ApiResponse;
import com.codealike.client.core.internal.dto.ProfileInfo;
import com.codealike.client.core.internal.dto.UserConfigurationInfo;
import com.codealike.client.core.internal.model.Profile;
import com.codealike.client.core.internal.model.TrackActivity;
import com.codealike.client.core.internal.startup.PluginContext;
import com.codealike.client.core.internal.utils.Configuration;
import com.codealike.client.core.internal.utils.LogManager;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;

public class IdentityService extends Observable {
	
	private static IdentityService _instance;
	private boolean isAuthenticated;
	private String identity;
	private String token;
	private Profile profile;
	private boolean credentialsStored;
	private TrackActivity trackActivities;
	
	public static IdentityService getInstance() {
		if (_instance == null) {
			_instance = new IdentityService();
		}
		
		return _instance;
	}
	
	public IdentityService() {
		this.identity = "";
		this.isAuthenticated = false;
		this.credentialsStored = false;
		this.token = "";
	}

	public boolean isAuthenticated() {
		return isAuthenticated;
	}

	public boolean login(String identity, String token, boolean storeCredentials, boolean rememberMe) {
		Notification note = new Notification("CodealikeApplicationComponent.Notifications",
				"Codealike",
				"Codealike  is connecting...",
				NotificationType.INFORMATION);
		Notifications.Bus.notify(note);

		if (this.isAuthenticated) {
			setChanged();
			notifyObservers();
			return true;
		}
		try {
			ApiClient apiClient = ApiClient.tryCreateNew(identity, token);
			ApiResponse<Void> response = apiClient.tokenAuthenticate();
			
			if (response.success()) {

				this.identity = identity;
				this.token = token;
				if (storeCredentials) {
					if (rememberMe) {
						storeCredentials(identity, token);
					}
					else {
						removeStoredCredentials();
					}
				}
				try {
					ApiResponse<ProfileInfo> profileResponse = apiClient.getProfile(identity);
					if (profileResponse.success()) {
						ProfileInfo profile = profileResponse.getObject();
						this.profile = new Profile(this.identity, profile.getFullName(), profile.getDisplayName(),
								profile.getAddress(), profile.getState(), profile.getCountry(), profile.getAvatarUri(), profile.getEmail());
					}
				}
				catch(Exception e) {
					LogManager.INSTANCE.logError(e, "Could not get user profile.");
				}

				try {
					ApiResponse<UserConfigurationInfo> configResponse = apiClient.getUserConfiguration(identity);
					if (configResponse.success()) {
						UserConfigurationInfo config = configResponse.getObject();
						this.trackActivities = config.getTrackActivities();
					}
				}
				catch(Exception e) {
					LogManager.INSTANCE.logError(e, "Could not get user configuration");
				}

				this.isAuthenticated = true;
				setChanged();
				notifyObservers();
				return true;
			}
			
		}
		catch (KeyManagementException e){
			LogManager.INSTANCE.logError(e, "Could not log in. There was a problem with SSL configuration.");
		}
		return false;
	}

	private void storeCredentials(String identity, String token) {
		// save user token to global configuration file
		Configuration configuration = PluginContext.getInstance().getConfiguration();
		configuration.setUserToken(identity + "/" + token);
		configuration.saveCurrentGlobalSettings();

		// remove fallback ones also!
		PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
		propertiesComponent.unsetValue("codealike.identity");
		propertiesComponent.unsetValue("codealike.token");
	}
	
	private void removeStoredCredentials() {
		Configuration configuration = PluginContext.getInstance().getConfiguration();
		configuration.setUserToken(null);
		configuration.saveCurrentGlobalSettings();

		// remove fallback ones also!
		PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
		propertiesComponent.unsetValue("codealike.identity");
		propertiesComponent.unsetValue("codealike.token");
	}

	public boolean tryLoginWithStoredCredentials() {
		Configuration configuration = PluginContext.getInstance().getConfiguration();
		String identity;
		String token;

		// if loaded configuration has no user token, try to fallback to previows store
		if (configuration.getUserToken() == null || configuration.getUserToken().isEmpty()) {
			// fallback information
			PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
			identity = propertiesComponent.getValue("codealike.identity", "");
			token = propertiesComponent.getValue("codealike.token", "");

			// if information found by fallback mechanism
			// lets save that configuration for future use
			if (identity != "" && token != "") {
				configuration.setUserToken(identity + "/" + token);
				configuration.saveCurrentGlobalSettings();

				// remove fallback ones also!
				propertiesComponent.unsetValue("codealike.identity");
				propertiesComponent.unsetValue("codealike.token");
			}
		}
		else {
			String[] split = configuration.getUserToken().split("/");
			identity = split[0];
			token = split[1];
		}

		if (identity != "" && token != "")
			return login(identity, token, false, false);

        return false;
	}

	public String getIdentity() {
		return identity;
	}

	public String getToken() {
		return token;
	}

	public Profile getProfile() {
		return profile;
	}

	public TrackActivity getTrackActivity() {
		return trackActivities;
	}

	public boolean isCredentialsStored() {
		return credentialsStored;
	}

	public void logOff() {
		Notification note = new Notification("CodealikeApplicationComponent.Notifications",
				"Codealike",
				"Codealike  is disconnecting...",
				NotificationType.INFORMATION);
		Notifications.Bus.notify(note);

		PluginContext.getInstance().getTrackingService().flushRecorder(this.identity, this.token);
		
		this.isAuthenticated = false;
		this.identity = null;
		this.token = null;
		removeStoredCredentials();
		
		setChanged();
		notifyObservers();
	}
}
