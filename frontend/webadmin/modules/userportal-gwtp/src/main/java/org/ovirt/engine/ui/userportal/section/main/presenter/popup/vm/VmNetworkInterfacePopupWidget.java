package org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm;

import org.ovirt.engine.ui.common.widget.uicommon.popup.networkinterface.NetworkInterfacePopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceModel;
import com.google.gwt.event.shared.EventBus;

public class VmNetworkInterfacePopupWidget extends NetworkInterfacePopupWidget {

    public VmNetworkInterfacePopupWidget(EventBus eventBus) {
        super(eventBus);
    }

    @Override
    public void edit(VmInterfaceModel iface) {
        super.edit(iface);
    }

}
