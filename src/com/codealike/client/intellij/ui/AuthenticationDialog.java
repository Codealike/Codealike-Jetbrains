package com.codealike.client.intellij.ui;

import com.codealike.client.core.internal.services.IdentityService;
import com.codealike.client.core.internal.startup.PluginContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by Daniel on 11/14/2016.
 */
public class AuthenticationDialog extends DialogWrapper {

    private JTextField authInput;
    private JLabel labelError;
    private Project _project;

    public AuthenticationDialog(Project project) {
        super(project, true);

        _project = project;

        setTitle("Codealike Authentication");

        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel();
        JLabel label = new JLabel();

        label.setText("Codealike Token:");

        labelError = new JLabel();
        labelError.setText("We couldn't authenticate you. Please verify your token and try again");
        labelError.setVisible(false);

        authInput = new JTextField(50);

        mainPanel.add(label);
        mainPanel.add(authInput);
        mainPanel.add(labelError);

        return mainPanel;
    }

    @Override
    protected void doOKAction() {
        IdentityService identityService = IdentityService.getInstance();

        labelError.setVisible(false);
        String[] split = authInput.getText().split("/");
        if (split.length == 2) {
            if(identityService.login(split[0], split[1], true, true)) {

                PluginContext.getInstance().getTrackingService().startTracking(_project);

                super.doOKAction();
            }
            else {
                labelError.setVisible(true);
            }
        }
        else {
            labelError.setVisible(true);
        }
    }
}
