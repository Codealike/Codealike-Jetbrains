package com.codealike.client.intellij;

import com.codealike.client.core.api.ApiClient;
import com.codealike.client.core.internal.dto.HealthInfo;
import com.codealike.client.core.internal.startup.PluginContext;
import com.codealike.client.intellij.ui.AuthenticationDialog;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.NotNull;
import java.security.KeyManagementException;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Daniel on 11/4/2016.
 */
public class CodealikeApplicationComponent implements ApplicationComponent {
    private PluginContext pluginContext;

    public CodealikeApplicationComponent() {
    }

    @Override
    public void initComponent() {
        // initialize plugin context with properties
        this.pluginContext = PluginContext.getInstance();

        try {
            pluginContext.initializeContext();

            pluginContext.getIdentityService().addObserver(loginObserver);
            if (!pluginContext.getIdentityService().tryLoginWithStoredCredentials()) {
                authenticate();
            }
        }
        catch (Exception e)
        {
            try {
                ApiClient client = ApiClient.tryCreateNew();
                client.logHealth(new HealthInfo(e, "Plugin could not start.", "intellij", HealthInfo.HealthInfoType.Error, pluginContext.getIdentityService().getIdentity()));
            }
            catch (KeyManagementException e1) {
                e1.printStackTrace();
            }
        }
    }


    @Override
    public void disposeComponent() {
        pluginContext.getLogger().log("Codealike component disposed");
    }

    @Override
    @NotNull
    public String getComponentName() {
        return "CodealikeApplicationComponent";
    }

    protected void authenticate() {
        ApplicationManager.getApplication().invokeLater(() -> {
                // prompt for apiKey if it does not already exist
                Project project = null;
                try {
                    project = ProjectManager.getInstance().getDefaultProject();
                } catch (NullPointerException e) { }

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

    Observer loginObserver = new Observer() {

        @Override
        public void update(Observable o, Object arg1) {
            if (o == pluginContext.getIdentityService()) {
                reloadOpenedProjects();
            }
        }
    };
}
