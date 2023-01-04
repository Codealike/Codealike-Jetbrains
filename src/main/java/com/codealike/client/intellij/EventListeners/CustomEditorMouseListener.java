/*
 * Copyright (c) 2022-2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.intellij.EventListeners;

import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseListener;

/**
 * Custom editor mouser listener.
 *
 * @author Daniel, pvmagacho
 * @version 1.5.0.2
 */
public class CustomEditorMouseListener implements EditorMouseListener {

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