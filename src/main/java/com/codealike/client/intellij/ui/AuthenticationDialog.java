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
 * Created by Daniel on 11/14/2016.
 */
public class AuthenticationDialog extends DialogWrapper {

    private JTextField authInput;
    private JLabel labelError;
    private JLabel labelMessage;
    private Project _project;

    public AuthenticationDialog(Project project) {
        super(project, true);

        _project = project;

        setTitle("Codealike Authentication");

        init();
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
        Configuration configuration = PluginContext.getInstance().getConfiguration();

        JPanel mainPanel = new JPanel();
        mainPanel.setMinimumSize(new Dimension(550, 100));
        mainPanel.setLayout(new GridLayout(4, 1));

        JLabel label = new JLabel();
        label.setText("Codealike Token:");

        labelError = new JLabel();
        labelError.setText("We couldn't authenticate you. Please verify your token and try again");
        labelError.setVisible(false);

        authInput = new JTextField(50);
        authInput.setText(configuration.getUserToken());

        labelMessage = new JLabel();
        labelMessage.setText("Find your Codealike API Token in your settings at Codealike Web");

        mainPanel.add(label);
        mainPanel.add(authInput);
        mainPanel.add(labelMessage);
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
