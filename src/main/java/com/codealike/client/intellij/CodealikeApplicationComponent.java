/*
 * Copyright (c) 2022-2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.intellij;

import com.codealike.client.core.api.ApiClient;
import com.codealike.client.core.internal.dto.HealthInfo;
import com.codealike.client.core.internal.services.ServiceListener;
import com.codealike.client.core.internal.startup.PluginContext;
import com.codealike.client.core.internal.utils.LogManager;
import com.codealike.client.intellij.ui.AuthenticationDialog;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.util.Properties;

/**
 * Plugin application component class.
 *
 * @author Daniel, pvmagacho
 * @version 1.6.0.0
 */
public class CodealikeApplicationComponent implements ApplicationComponent {
    private static final String CODEALIKE_PROPERTIES_FILE = "/codealike.properties";
    ServiceListener loginObserver = () -> reloadOpenedProjects();
    private PluginContext pluginContext;

    public CodealikeApplicationComponent() {
    }

    @Override
    public void initComponent() {
        // TODO: insert component initialization logic here
        LogManager.INSTANCE.logInfo("CodealikeApplicationComponent plugin initialized.");

        start();
    }

    @Override
    public void disposeComponent() {
        // TODO: insert component disposal logic here
    }

    @Override
    @NotNull
    public String getComponentName() {
        return "CodealikeApplicationComponent";
    }

    protected void start() {

        // load plugin properties
        Properties properties = new Properties();
        try {
            properties = loadPluginProperties();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // initialize plugin context with properties
        this.pluginContext = PluginContext.getInstance(properties);

        try {
            pluginContext.initializeContext();

            if (!pluginContext.checkVersion()) {
                throw new Exception();
            }

            pluginContext.getIdentityService().addListener(loginObserver);
            if (!pluginContext.getIdentityService().tryLoginWithStoredCredentials()) {
                authenticate();
            }
        } catch (Exception e) {
            try {
                ApiClient client = ApiClient.tryCreateNew();
                client.logHealth(new HealthInfo(e, "Plugin could not start.", "intellij", HealthInfo.HealthInfoType.Error, pluginContext.getIdentityService().getIdentity()));
            } catch (KeyManagementException e1) {
                e1.printStackTrace();
                LogManager.INSTANCE.logError(e, "Couldn't send HealtInfo.");
            }
            LogManager.INSTANCE.logError(e, "Couldn't start plugin.");
        }
    }

    protected Properties loadPluginProperties() throws IOException {
        Properties properties = new Properties();
        InputStream in = CodealikeApplicationComponent.class.getResourceAsStream(CODEALIKE_PROPERTIES_FILE);
        properties.load(in);
        in.close();

        return properties;
    }

    protected void authenticate() {
        ApplicationManager.getApplication().invokeLater(() -> {
            // prompt for apiKey if it does not already exist
            Project project = null;
            try {
                project = ProjectManager.getInstance().getDefaultProject();
            } catch (NullPointerException e) {
            }

            // lets ask for a api key
            AuthenticationDialog dialog = new AuthenticationDialog(project);
            dialog.show();
        });
    }

    private void reloadOpenedProjects() {
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        if (openProjects.length > 0) {
            for (Project p : openProjects) {
                ProjectManager.getInstance().reloadProject(p);
            }
        }
    }
}
