package org.ovirt.engine.ui.webadmin.section.main.view.popup.vm;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractVmPopupView;
import org.ovirt.engine.ui.common.view.popup.VmPopupResources;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.VmPopupWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VmPopupView extends AbstractVmPopupView implements VmPopupPresenterWidget.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<VmPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public VmPopupView(EventBus eventBus, VmPopupResources resources) {
        super(eventBus, new VmPopupWidget(), resources);
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}
