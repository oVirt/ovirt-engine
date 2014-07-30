package org.ovirt.engine.ui.webadmin.section.main.view.popup.vm;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundWidgetPopupView;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.VmRunOncePopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.RunOnceModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmRunOncePopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VmRunOncePopupView extends AbstractModelBoundWidgetPopupView<RunOnceModel> implements VmRunOncePopupPresenterWidget.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<VmRunOncePopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public VmRunOncePopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants, ApplicationMessages messages) {
        super(eventBus, resources, new VmRunOncePopupWidget(constants, resources, messages), "610px", "540px"); //$NON-NLS-1$ //$NON-NLS-2$
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}
