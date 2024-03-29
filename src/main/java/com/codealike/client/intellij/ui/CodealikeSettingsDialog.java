/*
 * Copyright (c) 2022-2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.intellij.ui;

import com.codealike.client.core.internal.services.IdentityService;
import com.codealike.client.core.internal.startup.PluginContext;
import com.codealike.client.core.internal.utils.Configuration;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Settings dialog. Used to set Codealike token.
 *
 * @author Daniel, pvmagacho
 * @version 1.6.0.0
 */
public class CodealikeSettingsDialog extends DialogWrapper {
    private JTextField tokenInput;
    private JLabel labelError;
    private JLabel labelWarning;
    private JButton forgetButton;
    private Project _project;

    /**
     * Constructor. Creates the Codelike settings dialog from the menu.
     *
     * @param project the current open {@link Project}
     */
    public CodealikeSettingsDialog(@Nullable Project project) {
        super(project, true);

        _project = project;

        setTitle("Codealike Settings");

        init();

        loadSettings();
    }

    @NotNull
    @Override
    protected Action[] createActions() {
        return new Action[]{
                myOKAction, myCancelAction
        };
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setMinimumSize(new Dimension(350, 100));
        mainPanel.setLayout(new GridLayout(4, 1));

        JLabel tokenLabel = new JLabel();
        tokenLabel.setText("Codealike Token:");

        labelWarning = new JLabel();
        labelWarning.setText("Projects will be reloaded if TOKEN is added, changed or removed.");
        labelWarning.setHorizontalAlignment(SwingConstants.CENTER);

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

        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(tokenLabel);
        inputPanel.add(tokenInput);

        mainPanel.add(inputPanel);

        JPanel warningPanel = new JPanel();
        warningPanel.add(labelWarning);
        mainPanel.add(warningPanel);

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

        Configuration configuration = PluginContext.getInstance().getConfiguration();
        tokenInput.setText(configuration.getUserToken());

        if (identityService.isAuthenticated() || identityService.isCredentialsStored()) {
            tokenInput.setEnabled(false);
            forgetButton.setVisible(true);
        } else {
            tokenInput.setEnabled(true);
            forgetButton.setVisible(false);
        }

        identityService.addListener(() -> {
            tokenInput.setText(configuration.getUserToken());
            if (identityService.isAuthenticated() || identityService.isCredentialsStored()) {
                tokenInput.setEnabled(false);
                forgetButton.setVisible(true);
            } else {
                tokenInput.setEnabled(true);
                forgetButton.setVisible(false);
            }
        });
    }
}
