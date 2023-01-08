package com.codealike.client.intellij.EventListeners;

import com.codealike.client.core.internal.services.TrackingService;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.VisibleAreaEvent;
import com.intellij.openapi.editor.event.VisibleAreaListener;

/**
 * Created by Daniel on 12/9/2016.
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
