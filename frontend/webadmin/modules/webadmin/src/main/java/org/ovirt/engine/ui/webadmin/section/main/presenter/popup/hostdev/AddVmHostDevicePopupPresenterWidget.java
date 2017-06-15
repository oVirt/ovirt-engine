package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.hostdev;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.hostdev.AddVmHostDevicesModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class AddVmHostDevicePopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<AddVmHostDevicesModel, AddVmHostDevicePopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<AddVmHostDevicesModel> {
    }

    @Inject
    public AddVmHostDevicePopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
