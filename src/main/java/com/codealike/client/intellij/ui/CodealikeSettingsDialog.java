/*
 * Copyright (c) 2022. All rights reserved to Torc LLC.
 */
package com.codealike.client.intellij.ui;

import com.codealike.client.core.internal.services.IdentityService;
import com.codealike.client.core.internal.startup.PluginContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Settings dialog. Used to set Codealike token.
 *
 * @author Daniel, pvmagacho
 * @version 1.5.0.2
 */
public class CodealikeSettingsDialog extends DialogWrapper {
    private JTextField tokenInput;
    private JLabel labelError;
    private JButton forgetButton;
    private Project _project;

    public CodealikeSettingsDialog(@Nullable Project project) {
        super(project, true);
        _project = project;
        setTitle("Codealike Settings");
        init();
        loadSettings();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel();

        JLabel tokenLabel = new JLabel();
        tokenLabel.setText("Codealike Token:");

        labelError = new JLabel();
        labelError.setText("We couldn't authenticate you. Please verify your token and try again");
        labelError.setVisible(false);

        tokenInput = new JTextField(50);
        tokenInput.setEnabled(false);

        forgetButton = new JButton();
        forgetButton.setText("I want to remove/change my token on this computer");
        forgetButton.addActionListener(e -> {
            // log off
            IdentityService.getInstance().logOff();
        });

        mainPanel.add(tokenLabel);
        mainPanel.add(tokenInput);
        mainPanel.add(labelError);
        mainPanel.add(forgetButton);

        return mainPanel;
    }

    @Override
    protected void doOKAction() {
        IdentityService identityService = IdentityService.getInstance();
        labelError.setVisible(false);
        String[] split = tokenInput.getText().split("/");
        if (split.length == 2) {
            if (identityService.login(split[0], split[1], true, true)) {
                PluginContext.getInstance().getTrackingService().startTracking(_project);
                super.doOKAction();
            } else {
                labelError.setVisible(true);
            }
        } else {
            labelError.setVisible(true);
        }
    }

    private void loadSettings() {
        IdentityService identityService = IdentityService.getInstance();
        if (identityService.isAuthenticated() || identityService.isCredentialsStored()) {
            tokenInput.setEnabled(false);
            forgetButton.setVisible(true);
            tokenInput.setText(identityService.getIdentity() + "/" + identityService.getToken());
        } else {
            tokenInput.setText("");
            tokenInput.setEnabled(true);
            forgetButton.setVisible(false);
        }

        identityService.addListener(() -> {
            if (identityService.isAuthenticated() || identityService.isCredentialsStored()) {
                tokenInput.setEnabled(false);
                forgetButton.setVisible(true);
                tokenInput.setText(identityService.getIdentity() + "/" + identityService.getToken());
            } else {
                tokenInput.setText("");
                tokenInput.setEnabled(true);
                forgetButton.setVisible(false);
            }
        });
    }

}
