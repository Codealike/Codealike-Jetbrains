/*
 * Copyright (c) 2022-2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.intellij.ui;

import com.codealike.client.core.internal.services.IdentityService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Authentication dialog. Used to set the Codealike token.
 *
 * @author Daniel, pvmagacho
 * @version 1.5.0.2
 */
public class AuthenticationDialog extends DialogWrapper {

    private JTextField authInput;
    private JLabel labelError;

    /**
     * Constructor. Creates the authentication dialog when running the plugin for the first time.
     *
     * @param project the current open {@link Project}
     */
    public AuthenticationDialog(Project project) {
        super(project, true);
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
            if (identityService.login(split[0], split[1], true, true)) {
                super.doOKAction();
            } else {
                labelError.setVisible(true);
            }
        } else {
            labelError.setVisible(true);
        }
    }
}
