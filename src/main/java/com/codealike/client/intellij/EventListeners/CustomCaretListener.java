/*
 * Copyright (c) 2022. All rights reserved to Torc LLC.
 */
package com.codealike.client.intellij.EventListeners;

import com.codealike.client.core.internal.services.TrackingService;
import com.codealike.client.core.internal.startup.PluginContext;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;

/**
 * Custom caret listener.
 *
 * @author Daniel, pvmagacho
 * @version 1.5.0.2
 */
public class CustomCaretListener implements CaretListener {

    @Override
    public void caretPositionChanged(CaretEvent caretEvent) {
        Document document = caretEvent.getEditor().getDocument();
        if (document != null && FileDocumentManager.getInstance().getFile(document) != null) {
            TrackingService trackingService = PluginContext.getInstance().getTrackingService();
            Editor editor = caretEvent.getEditor();
            trackingService.trackDocumentFocus(editor, caretEvent.getCaret().getOffset());
        }
    }

    @Override
    public void caretAdded(CaretEvent caretEvent) {
    }

    @Override
    public void caretRemoved(CaretEvent caretEvent) {
    }
}
