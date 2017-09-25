package com.codealike.client.core.internal.utils;

import com.codealike.client.core.internal.model.GlobalSettings;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Optional;

public class Configuration {
    private ObjectMapper mapper = new ObjectMapper();
    private GlobalSettings globalSettings = new GlobalSettings();
    private File codealikeBasePath;

    private String clientId;
    private String clientVersion;
    private String instanceId;

    public Configuration(String clientId, String clientVersion, String instanceId) {
        // sets the codealike base path for logging and user settings and profile
        File basePath = new File(System.getProperty("user.home"), ".codealike");

        // store current instance settings
        this.clientId = clientId;
        this.clientVersion = clientVersion;
        this.instanceId = instanceId;

        // ensure codealike base path exists
        if (!Files.exists(basePath.toPath())) {
            try {
                Files.createDirectories(basePath.toPath());
            }
            catch(IOException ex) {
                // check what to do if this fails
            }
        }

        // sets path for aplication usage
        this.codealikeBasePath = basePath;
    }

    /*
     *  loadCodealikeSettings:
     *  This method loads user settings stored in codealike user folder
     *  After this method call userToken and user profile information
     *  should been loaded
     */
    public void loadGlobalSettings() {
        File codealikeSettingsFile = new File(this.codealikeBasePath, "user.json");

        try {
            if (Files.exists(codealikeSettingsFile.toPath())) {
                ObjectMapper mapper = new ObjectMapper();
                GlobalSettings existingConfiguration = mapper.readValue(new FileInputStream(codealikeSettingsFile), GlobalSettings.class);

                if (existingConfiguration != null) {
                    this.globalSettings.setUserToken(existingConfiguration.getUserToken());
                    this.globalSettings.setApiUrl(Optional.ofNullable(existingConfiguration.getApiUrl()).orElse("https://codealike.com/api/v2"));
                }
                else {
                    this.globalSettings.setUserToken(null);
                    this.globalSettings.setApiUrl("https://codealike.com/api/v2");
                }
            }
        }
        catch(IOException exception) {
            // check what to do if this fails
        }
    }

    public void savelGlobalSettings(GlobalSettings settings) {
        File codealikeSettingsFile = new File(this.codealikeBasePath, "user.json");

        String jsonString = null;
        FileOutputStream stream = null;
        try {
            // convert object to string
            jsonString = mapper.writeValueAsString(this.globalSettings);

            // if registered, save configuration file
            // have to save configuration file
            stream = new FileOutputStream(codealikeSettingsFile);
            stream.write(jsonString.getBytes(Charset.forName("UTF-8")));
            stream.close();
        }
        catch(JsonProcessingException jsonEx) {
            // check what to do if this fails
        }
        catch(IOException exception) {
            // check what to do if this fails
        }
        finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // check what to do if this fails
                    e.printStackTrace();
                }
            }
        }
    }

    public void setUserToken(String userToken) {
        this.globalSettings.setUserToken(userToken);
    }

    public String getUserToken() {
        return this.globalSettings.getUserToken();
    }
}
