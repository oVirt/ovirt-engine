package org.ovirt.engine.ui.common.widget.dialog;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.DialogBox;

public class DialogBoxWithKeyHandlers extends DialogBox {

    private PopupNativeKeyPressHandler keyPressHandler;

    public void setKeyPressHandler(PopupNativeKeyPressHandler keyPressHandler) {
        this.keyPressHandler = keyPressHandler;
    }

    @Override
    protected void onPreviewNativeEvent(NativePreviewEvent event) {
        super.onPreviewNativeEvent(event);

        if (keyPressHandler != null && event.getTypeInt() == Event.ONKEYPRESS) {
            keyPressHandler.onKeyPress(event.getNativeEvent());
        }
    }

}
