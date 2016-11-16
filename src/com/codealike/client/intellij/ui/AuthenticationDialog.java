package com.codealike.client.intellij.ui;

import com.codealike.client.core.internal.model.Profile;
import com.codealike.client.core.internal.services.IIdentityService;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by Daniel on 11/14/2016.
 */
public class AuthenticationDialog extends DialogWrapper {

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
        JTextField authInput = new JTextField(50);
        panel.add(label);
        panel.add(authInput);
        return panel;
    }

    @Override
    protected void doOKAction() {
        IIdentityService identityService = ServiceManager.getService(IIdentityService.class);

        // get information from text field and validate format
        //danieltest/52d46562-e153-4c57-99bc-14aeca205dba

        boolean logged = identityService.login("danieltest", "52d46562-e153-4c57-99bc-14aeca205dba", false, false);

        Profile profile = identityService.getProfile();

        super.doOKAction();
    }
}
