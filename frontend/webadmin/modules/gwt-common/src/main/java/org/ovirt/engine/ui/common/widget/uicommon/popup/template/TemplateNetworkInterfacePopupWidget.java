package org.ovirt.engine.ui.common.widget.uicommon.popup.template;

import org.ovirt.engine.ui.common.widget.uicommon.popup.networkinterface.NetworkInterfacePopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceModel;
import com.google.gwt.event.shared.EventBus;

public class TemplateNetworkInterfacePopupWidget extends NetworkInterfacePopupWidget {

    public TemplateNetworkInterfacePopupWidget(EventBus eventBus) {
        super(eventBus);
    }

    @Override
    public void edit(VmInterfaceModel iface) {
        super.edit(iface);
    }

}
