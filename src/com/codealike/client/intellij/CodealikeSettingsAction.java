package com.codealike.client.intellij;

import com.codealike.client.intellij.ui.CodealikeSettingsDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Messages;

/**
 * Created by Daniel on 12/7/2016.
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
