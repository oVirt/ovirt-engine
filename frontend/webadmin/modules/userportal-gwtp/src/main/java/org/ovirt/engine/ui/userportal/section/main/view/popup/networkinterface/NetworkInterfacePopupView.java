package org.ovirt.engine.ui.userportal.section.main.view.popup.networkinterface;

import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundWidgetPopupView;
import org.ovirt.engine.ui.common.widget.uicommon.popup.networkinterface.NetworkInterfacePopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceModel;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.networkinterface.NetworkInterfacePopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class NetworkInterfacePopupView extends AbstractModelBoundWidgetPopupView<VmInterfaceModel> implements NetworkInterfacePopupPresenterWidget.ViewDef {

    @Inject
    public NetworkInterfacePopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus,
                resources,
                new NetworkInterfacePopupWidget(eventBus, constants),
                "400px",
                "320px");
    }
}
