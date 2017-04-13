package org.ovirt.engine.ui.common.widget.editor;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;

/**
 * If this handler is registered to focus and blur events of a widget, the enter key will be ignored while
 * this widget has focus. The "enter will be ignored" means that any listener listening to enter will not be
 * notifyed so the dialog will not get submitted.
 *
 * It is useful for widgets like textarea where the user should be able to press enter without submitting the dialog.
 */
public class EnterIgnoringFocusHandler implements FocusHandler, BlurHandler {

    private HandlerRegistration eventHandler;

    @Override
    public void onFocus(FocusEvent event) {
        eventHandler = Event.addNativePreviewHandler(e -> {
            NativeEvent nativeEvent = e.getNativeEvent();
            if (nativeEvent.getKeyCode() == KeyCodes.KEY_ENTER
                    && (e.getTypeInt() == Event.ONKEYPRESS || e.getTypeInt() == Event.ONKEYDOWN)
                    && !e.isCanceled()) {

                // swallow the enter key otherwise the whole dialog would get submitted
                nativeEvent.preventDefault();
                nativeEvent.stopPropagation();
                e.cancel();

                if (e.getTypeInt() == Event.ONKEYDOWN) {
                    enterPressed();
                }
            }
        });
    }

    @Override
    public void onBlur(BlurEvent event) {
        if (eventHandler != null) {
            eventHandler.removeHandler();
        }
    }

    protected void enterPressed() {
        // any custom operation
    }
}
