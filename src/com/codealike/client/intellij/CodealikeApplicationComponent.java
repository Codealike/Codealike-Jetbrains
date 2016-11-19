package com.codealike.client.intellij;

import com.codealike.client.core.api.ApiClient;
import com.codealike.client.core.internal.dto.HealthInfo;
import com.codealike.client.core.internal.services.IdentityService;
import com.codealike.client.core.internal.services.TrackingService;
import com.codealike.client.core.internal.startup.PluginContext;
import com.codealike.client.core.internal.utils.LogManager;
import com.codealike.client.intellij.EventListeners.CustomCaretListener;
import com.codealike.client.intellij.EventListeners.CustomDocumentListener;
import com.codealike.client.intellij.ui.AuthenticationDialog;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

/**
 * Created by Daniel on 11/4/2016.
 */
public class CodealikeApplicationComponent implements ApplicationComponent {
    private static final String CODEALIKE_PROPERTIES_FILE = "codealike.properties";

    private PluginContext pluginContext;

    public CodealikeApplicationComponent() {
    }

    @Override
    public void initComponent() {
        // TODO: insert component initialization logic here
        LogManager.INSTANCE.logInfo("CodealikeApplicationComponent plugin initialized.");

        start();

        Notification note = new Notification("CodealikeApplicationComponent.Notifications",
                "CodealikeApplicationComponent",
                "Levanto la aplicacion",
                NotificationType.INFORMATION);
        Notifications.Bus.notify(note);

        /*
        setupQueueProcessor();

        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();

        String token = propertiesComponent.getValue("codealike.token", "");

        if (token == "") {
            // ask user for a valid token
            token = "F8D0D2EF-DDBE-4C97-910B-6BC935AFD320";

            propertiesComponent.setValue("codealike.token", token);
        }

        log.debug("Token: " + token);
        */

        //setupEventListeners();
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
        Properties properties = new Properties();

        try {
            properties = loadPluginProperties();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.pluginContext = PluginContext.getInstance(properties);

        try {
            pluginContext.initializeContext();

            if (!pluginContext.checkVersion()) {
                throw new Exception();
            }

            pluginContext.getTrackingService().setBeforeOpenProjectDate();
            pluginContext.getIdentityService().addObserver(loginObserver);
            if (!pluginContext.getIdentityService().tryLoginWithStoredCredentials()) {
                authenticate();
            }
            else {
                startTracker();
            }
        }
        catch (Exception e)
        {
            try {
                ApiClient client = ApiClient.tryCreateNew();
                client.logHealth(new HealthInfo(e, "Plugin could not start.", "eclipse", HealthInfo.HealthInfoType.Error, pluginContext.getIdentityService().getIdentity()));
            }
            catch (KeyManagementException e1) {
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

    protected void startTracker() {
        pluginContext.getTrackingService().startTracking();
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

    private void setupEventListeners() {
        ApplicationManager.getApplication().invokeLater(() -> {
            // edit document
            EditorFactory
                    .getInstance()
                    .getEventMulticaster()
                    .addDocumentListener(new CustomDocumentListener());

            EditorFactory
                    .getInstance()
                    .getEventMulticaster()
                    .addCaretListener(new CustomCaretListener());
        });
    }

    Observer loginObserver = new Observer() {

        @Override
        public void update(Observable o, Object arg1) {
            Notification note;
            if (o == pluginContext.getIdentityService()) {
                TrackingService trackingService = pluginContext.getTrackingService();
                IdentityService identityService = pluginContext.getIdentityService();
                if (identityService.isAuthenticated()) {
                    switch(identityService.getTrackActivity()) {
                        case Always:
                        {
                            trackingService.enableTracking();
                            break;
                        }
                        case AskEveryTime:
                        case Never:
                            //WorkbenchUtils.addMessageToStatusBar("CodealikeApplicationComponent is not tracking your projects");
                            note = new Notification("CodealikeApplicationComponent.Notifications",
                                    "CodealikeApplicationComponent",
                                    "Codealike  is not tracking your projects",
                                    NotificationType.INFORMATION);
                            Notifications.Bus.notify(note);
                            break;
                    }
                }
                else {
                    trackingService.disableTracking();
                }
            }
        }
    };
}
