package org.ovirt.engine.ui.webadmin.section.main.view.popup.vm;

import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractVmPopupView;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.VmDesktopNewPopupWidget;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmDesktopNewPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VmDesktopNewPopupView extends AbstractVmPopupView implements VmDesktopNewPopupPresenterWidget.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<VmDesktopNewPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public VmDesktopNewPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants, CommonApplicationMessages messages, CommonApplicationTemplates templates) {
        super(eventBus, resources, new VmDesktopNewPopupWidget(constants, resources, messages, templates));
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}
