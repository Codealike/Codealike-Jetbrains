/*
 * Copyright (c) 2022. All rights reserved to Torc LLC.
 */
package com.codealike.client.intellij.EventListeners;

import com.codealike.client.core.internal.services.TrackingService;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;

/**
 * Custom document listener.
 *
 * @author Daniel, pvmagacho
 * @version 1.5.0.2
 */
public class CustomDocumentListener implements DocumentListener {
    @Override
    public void beforeDocumentChange(DocumentEvent documentEvent) {
    }

    @Override
    public void documentChanged(DocumentEvent documentEvent) {
        final Document document = documentEvent.getDocument();

        if (document != null && FileDocumentManager.getInstance().getFile(document) != null) {
            final Editor[] editors = EditorFactory.getInstance().getEditors(document);

            if (editors.length > 0) {
                TrackingService.getInstance().trackCodingEvent(editors[0], documentEvent.getOffset());
            }
        }
    }
}
