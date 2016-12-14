package com.codealike.client.intellij.EventListeners;

import com.codealike.client.core.internal.model.ActivityEvent;
import com.codealike.client.core.internal.services.TrackingService;
import com.codealike.client.core.internal.startup.PluginContext;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;

import java.util.UUID;

/**
 * Created by Daniel on 11/4/2016.
 */
public class CustomCaretListener implements CaretListener {

    @Override
    public void caretPositionChanged(CaretEvent caretEvent) {
        Document document = caretEvent.getEditor().getDocument();
        if (document != null && FileDocumentManager.getInstance().getFile(document) != null) {
            TrackingService trackingService = PluginContext.getInstance().getTrackingService();
            Editor editor = caretEvent.getEditor();
            trackingService.trackCodingEvent(editor, caretEvent.getCaret().getOffset());
        }
    }

    @Override
    public void caretAdded(CaretEvent caretEvent) {
        Document document = caretEvent.getEditor().getDocument();
        if (document != null && FileDocumentManager.getInstance().getFile(document) != null) {
            TrackingService trackingService = PluginContext.getInstance().getTrackingService();
            Editor editor = caretEvent.getEditor();
            trackingService.trackCodingEvent(editor, caretEvent.getCaret().getOffset());
        }
    }

    @Override
    public void caretRemoved(CaretEvent caretEvent) {
        Document document = caretEvent.getEditor().getDocument();
        if (document != null && FileDocumentManager.getInstance().getFile(document) != null) {
            TrackingService trackingService = PluginContext.getInstance().getTrackingService();
            Editor editor = caretEvent.getEditor();
            trackingService.trackCodingEvent(editor, caretEvent.getCaret().getOffset());
        }
    }
}
