package com.codealike.client.intellij.EventListeners;

import com.codealike.client.core.internal.services.TrackingService;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;

/**
 * Created by Daniel on 12/9/2016.
 */
public class CustomEditorMouseListener  implements EditorMouseListener {

    @Override
    public void mousePressed(EditorMouseEvent editorMouseEvent) {
        /*final Editor editor = editorMouseEvent.getEditor();
        final int offset = editorMouseEvent.getEditor().getCaretModel().getOffset();

        if (editor != null) {
            Document document = editor.getDocument();
            if (document != null && FileDocumentManager.getInstance().getFile(document) != null) {
                TrackingService.getInstance().trackDocumentFocus(editor, offset);
            }
        }*/
    }

    @Override
    public void mouseClicked(EditorMouseEvent editorMouseEvent) {
    }

    @Override
    public void mouseReleased(EditorMouseEvent editorMouseEvent) {
    }

    @Override
    public void mouseEntered(EditorMouseEvent editorMouseEvent) {
    }

    @Override
    public void mouseExited(EditorMouseEvent editorMouseEvent) {
    }
}