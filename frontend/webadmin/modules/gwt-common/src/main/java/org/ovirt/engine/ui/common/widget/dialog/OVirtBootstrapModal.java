package org.ovirt.engine.ui.common.widget.dialog;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.PopupPanel;

public class OVirtBootstrapModal extends PopupPanel {

    private PopupNativeKeyPressHandler keyPressHandler;

    public HandlerRegistration setKeyPressHandler(PopupNativeKeyPressHandler keyPressHandler) {
        this.keyPressHandler = keyPressHandler;
        return () -> OVirtBootstrapModal.this.keyPressHandler = null;
    }

    @Override
    protected void onPreviewNativeEvent(NativePreviewEvent event) {
        super.onPreviewNativeEvent(event);
        if (keyPressHandler != null && event.getTypeInt() == Event.ONKEYDOWN) {
            keyPressHandler.onKeyPress(event.getNativeEvent());
        }
    }

}
