package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VmInterfacePopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<VmInterfaceModel, VmInterfacePopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<VmInterfaceModel> {
    }

    @Inject
    public VmInterfacePopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
