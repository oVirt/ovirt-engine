package org.ovirt.engine.ui.webadmin.section.main.view.popup.vm;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmDiskAttachPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VmDiskAttachPopupView extends BaseVmDiskAttachPopupView implements VmDiskAttachPopupPresenterWidget.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<VmDiskAttachPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public VmDiskAttachPopupView(EventBus eventBus) {
        super(eventBus, true);
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }
}
