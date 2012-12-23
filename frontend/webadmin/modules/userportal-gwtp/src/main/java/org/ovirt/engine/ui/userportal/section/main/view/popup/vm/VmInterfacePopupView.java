package org.ovirt.engine.ui.userportal.section.main.view.popup.vm;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundWidgetPopupView;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceModel;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationResources;
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
    public VmInterfacePopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus,
                resources,
                new VmNetworkInterfacePopupWidget(eventBus, constants), "510px", //$NON-NLS-1$
                "345px"); //$NON-NLS-1$
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}
