package com.codealike.client.intellij;

import com.codealike.client.core.internal.services.IIdentityService;
import com.codealike.client.intellij.ui.AuthenticationDialog;
import com.codealike.client.intellij.ui.CodealikeSettingsDialog;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.options.newEditor.SettingsDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * Created by Daniel on 11/4/2016.
 */
public class CodealikeApplicationComponent implements ApplicationComponent {

    public static final Logger log = Logger.getInstance("CodealikeApplicationComponent");
    /*private static final UUID _token = null;

    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static ScheduledFuture<?> scheduledFixture;*/

    public CodealikeApplicationComponent() {
    }

    @Override
    public void initComponent() {
        // TODO: insert component initialization logic here

        log.info("CodealikeApplicationComponent plugin initialized.");

        Project project = ProjectManager.getInstance().getDefaultProject();

        Notification note = new Notification("CodealikeApplicationComponent.Notifications",
                "CodealikeApplicationComponent",
                "Levanto la aplicacion",
                NotificationType.INFORMATION);
        Notifications.Bus.notify(note);

        AuthenticationDialog dialog = new AuthenticationDialog(project);
        dialog.show();

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

        setupEventListeners();
        */
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

    /*
    private void setupQueueProcessor() {
        final Runnable handler = new Runnable() {
            public void run() {

            }
        };
        long delay = 10;
        scheduledFixture = scheduler.scheduleAtFixedRate(handler, delay, delay, java.util.concurrent.TimeUnit.SECONDS);
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
    */
}
