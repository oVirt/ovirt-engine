package org.ovirt.engine.ui.userportal.section.main.presenter.popup.networkinterface;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class NetworkInterfacePopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<VmInterfaceModel, NetworkInterfacePopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<VmInterfaceModel> {
    }

    @Inject
    public NetworkInterfacePopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
