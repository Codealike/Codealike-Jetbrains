/*
 * Copyright (c) 2022-2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.intellij;

import com.codealike.client.intellij.ui.CodealikeSettingsDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;

/**
 * Plugin settings action.
 *
 * @author Daniel, pvmagacho
 * @version 1.6.0.0
 */
public class CodealikeSettingsAction extends AnAction {

    public CodealikeSettingsAction() {
        super("Codealike");
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        Project project = e.getData(PlatformDataKeys.PROJECT);
        CodealikeSettingsDialog settingsDialog = new CodealikeSettingsDialog(project);
        settingsDialog.show();
    }
}
