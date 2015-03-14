package org.ovirt.engine.ui.webadmin.section.main.view.popup.vm;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.SingleSelectionVmDiskAttachPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SingleSelectionVmDiskAttachPopupView extends BaseVmDiskAttachPopupView implements SingleSelectionVmDiskAttachPopupPresenterWidget.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SingleSelectionVmDiskAttachPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SingleSelectionVmDiskAttachPopupView(EventBus eventBus) {
        super(eventBus, false);
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }
}
