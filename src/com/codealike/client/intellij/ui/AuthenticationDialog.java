package com.codealike.client.intellij.ui;

import com.codealike.client.core.internal.services.IdentityService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by Daniel on 11/14/2016.
 */
public class AuthenticationDialog extends DialogWrapper {

    private JTextField authInput;

    public AuthenticationDialog(Project project) {
        super(project, true);

        setTitle("CodealikeApplicationComponent Authentication");

        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel();
        JLabel label = new JLabel();
        label.setText("Ingresa tu token de Codealike:");
        authInput = new JTextField(50);
        panel.add(label);
        panel.add(authInput);
        return panel;
    }

    @Override
    protected void doOKAction() {
        IdentityService identityService = IdentityService.getInstance();

        String[] split = authInput.getText().split("/");
        if (split.length == 2) {
            identityService.login(split[0], split[1], true, true);
        }

        super.doOKAction();
    }
}
