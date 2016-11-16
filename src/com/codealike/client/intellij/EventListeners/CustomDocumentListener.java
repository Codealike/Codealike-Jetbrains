package com.codealike.client.intellij.EventListeners;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;

/**
 * Created by Daniel on 11/4/2016.
 */
public class CustomDocumentListener implements DocumentListener {
    @Override
    public void beforeDocumentChange(DocumentEvent documentEvent) {
    }

    @Override
    public void documentChanged(DocumentEvent documentEvent) {
        final FileDocumentManager instance = FileDocumentManager.getInstance();
        final Document document = documentEvent.getDocument();
        final VirtualFile file = instance.getFile(document);

        final DataContext dataContext = DataManager.getInstance().getDataContext();

        final Project project = PlatformDataKeys.PROJECT.getData(dataContext);

        final PsiFile psiFil = PsiDocumentManager
                .getInstance(project).getPsiFile(document);

        if (file != null) {
            final String currentFile = file.getPath();
           /* if (WakaTime.shouldLogFile(currentFile)) {
                BigDecimal currentTime = WakaTime.getCurrentTimestamp();
                if (!currentFile.equals(WakaTime.lastFile) || WakaTime.enoughTimePassed(currentTime)) {
                    WakaTime.appendHeartbeat(currentTime, currentFile, false);
                }
            }*/
        }
    }
}
