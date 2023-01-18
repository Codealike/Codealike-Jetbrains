/*
 * Copyright (c) 2022-2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.intellij.EventListeners;

import com.codealike.client.core.internal.services.TrackingService;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.VisibleAreaEvent;
import com.intellij.openapi.editor.event.VisibleAreaListener;

/**
 * Custom visible area listener.
 *
 * @author Daniel, pvmagacho
 * @version 1.6.0.0
 */
public class CustomVisibleAreaListener implements VisibleAreaListener {
    @Override
    public void visibleAreaChanged(VisibleAreaEvent visibleAreaEvent) {
        final Editor editor = visibleAreaEvent.getEditor();
        final int offset = visibleAreaEvent.getEditor().getCaretModel().getOffset();

        if (editor != null) {
            TrackingService.getInstance().trackDocumentFocus(
                    editor,
                    offset,
                    editor.offsetToLogicalPosition(offset).line);
        }
    }
}
