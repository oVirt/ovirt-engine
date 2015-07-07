package org.ovirt.engine.ui.userportal.section.main.view.popup.vm;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundWidgetPopupView;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceModel;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmInterfacePopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmNetworkInterfacePopupWidget;
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
                new VmNetworkInterfacePopupWidget(eventBus), "510px", //$NON-NLS-1$
                "320px"); //$NON-NLS-1$
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}
