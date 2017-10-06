package org.ovirt.engine.ui.webadmin.section.main.view.popup.vm;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundWidgetPopupView;
import org.ovirt.engine.ui.common.widget.uicommon.popup.networkinterface.NetworkInterfacePopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmInterfacePopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VmInterfacePopupView extends AbstractModelBoundWidgetPopupView<VmInterfaceModel> implements VmInterfacePopupPresenterWidget.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<VmInterfacePopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public VmInterfacePopupView(EventBus eventBus) {
        super(eventBus,
                new NetworkInterfacePopupWidget(eventBus), "650px", //$NON-NLS-1$
                "515px"); //$NON-NLS-1$
        ViewIdHandler.idHandler.generateAndSetIds(this);

    }

}
