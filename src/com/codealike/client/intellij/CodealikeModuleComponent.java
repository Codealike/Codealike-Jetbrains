package com.codealike.client.intellij;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Daniel on 11/16/2016.
 */
public class CodealikeModuleComponent implements ModuleComponent {
    private Module _module = null;

    public CodealikeModuleComponent(Module module) {
        _module = module;
    }

    @Override
    public void initComponent() {
        // TODO: insert component initialization logic here
    }

    @Override
    public void disposeComponent() {
        // TODO: insert component disposal logic here
    }

    @Override
    @NotNull
    public String getComponentName() {
        return "CodealikeModuleComponent";
    }

    @Override
    public void projectOpened() {
        // called when project is opened
    }

    @Override
    public void projectClosed() {
        // called when project is being closed
    }

    @Override
    public void moduleAdded() {
        // Invoked when the module corresponding to this component instance has been completely
        // loaded and added to the project.
    }
}
