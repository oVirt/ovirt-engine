package org.ovirt.engine.ui.userportal.section.main.view.popup.vm;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.ApplicationTemplates;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.SingleSelectionVmDiskAttachPopupPresenterWidget;

public class SingleSelectionVmDiskAttachPopupView extends BaseVmDiskAttachPopupView implements SingleSelectionVmDiskAttachPopupPresenterWidget.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SingleSelectionVmDiskAttachPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SingleSelectionVmDiskAttachPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants, ApplicationTemplates templates) {
        super(eventBus, resources, constants, templates, false);
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}
