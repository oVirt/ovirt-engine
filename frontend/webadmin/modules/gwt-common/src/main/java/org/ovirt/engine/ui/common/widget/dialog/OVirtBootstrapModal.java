package org.ovirt.engine.ui.common.widget.dialog;

import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.user.client.ui.PopupPanel;

public class OVirtBootstrapModal extends PopupPanel {

    public void setKeyPressHandler(PopupNativeKeyPressHandler keyPressHandler) {
        addDomHandler(event -> keyPressHandler.onKeyPress(event.getNativeEvent()),
                KeyPressEvent.getType());
    }

}
