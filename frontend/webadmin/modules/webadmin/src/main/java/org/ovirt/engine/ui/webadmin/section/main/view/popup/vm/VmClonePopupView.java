package org.ovirt.engine.ui.webadmin.section.main.view.popup.vm;

import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractVmPopupView;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.VmClonePopupWidget;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmClonePopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VmClonePopupView extends AbstractVmPopupView implements VmClonePopupPresenterWidget.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<VmClonePopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public VmClonePopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants, CommonApplicationMessages messages, CommonApplicationTemplates templates) {
        super(eventBus, resources, new VmClonePopupWidget(constants, resources, messages, templates));
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}
