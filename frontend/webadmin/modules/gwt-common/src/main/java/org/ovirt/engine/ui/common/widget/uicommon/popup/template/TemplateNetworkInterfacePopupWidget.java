package org.ovirt.engine.ui.common.widget.uicommon.popup.template;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.widget.uicommon.popup.networkinterface.NetworkInterfacePopupWidget;

import com.google.gwt.event.shared.EventBus;

public class TemplateNetworkInterfacePopupWidget extends
        NetworkInterfacePopupWidget {

    public TemplateNetworkInterfacePopupWidget(EventBus eventBus,
            CommonApplicationConstants constants) {
        super(eventBus, constants);
        activateCheckBox.setVisible(false);
    }
}
