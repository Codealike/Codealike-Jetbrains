package com.codealike.client.intellij.EventListeners;

import com.codealike.client.core.internal.model.ActivityEvent;
import com.codealike.client.core.internal.services.TrackingService;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;

import java.util.UUID;

/**
 * Created by Daniel on 11/4/2016.
 */
public class CustomDocumentListener implements DocumentListener {
    @Override
    public void beforeDocumentChange(DocumentEvent documentEvent) {
        //final Document document = documentEvent.getDocument();

        //if (document != null) {
        //    final Editor[] editors = EditorFactory.getInstance().getEditors(document);

        //    if (editors.length > 0) {
        //        TrackingService.getInstance().trackDocumentFocus(editors[0]);
        //    }
        //}
    }

    @Override
    public void documentChanged(DocumentEvent documentEvent) {
        final Document document = documentEvent.getDocument();

        if (document != null) {
            final Editor[] editors = EditorFactory.getInstance().getEditors(document);

            if (editors.length > 0) {
                TrackingService.getInstance().trackDocumentFocus(editors[0], documentEvent.getOffset());
            }
        }
    }
}
