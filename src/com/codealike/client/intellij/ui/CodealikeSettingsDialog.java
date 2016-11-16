package com.codealike.client.intellij.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by Daniel on 11/14/2016.
 */
public class CodealikeSettingsDialog extends DialogWrapper {

    public CodealikeSettingsDialog(@Nullable Project project) {
        super(project, true);

        setTitle("CodealikeApplicationComponent Settings");

        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return null;
    }
}
