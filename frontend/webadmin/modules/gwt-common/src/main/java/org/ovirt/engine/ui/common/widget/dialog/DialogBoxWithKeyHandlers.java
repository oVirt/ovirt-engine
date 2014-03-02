package org.ovirt.engine.ui.common.widget.dialog;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;

public class DialogBoxWithKeyHandlers extends ResizableDialogBox {

    private PopupNativeKeyPressHandler keyPressHandler;

    public void setKeyPressHandler(PopupNativeKeyPressHandler keyPressHandler) {
        this.keyPressHandler = keyPressHandler;
    }

    @Override
    protected void onPreviewNativeEvent(NativePreviewEvent event) {
        super.onPreviewNativeEvent(event);

        if (keyPressHandler != null && event.getTypeInt() == Event.ONKEYDOWN) {
            keyPressHandler.onKeyPress(event.getNativeEvent());
        }
    }

}
