package org.ovirt.engine.ui.webadmin.section.main.view.popup.vm;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundWidgetPopupView;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.VmNextRunConfigurationWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmNextRunConfigurationModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmNextRunConfigurationPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VmNextRunConfigurationPopupView extends AbstractModelBoundWidgetPopupView<VmNextRunConfigurationModel>
        implements VmNextRunConfigurationPresenterWidget.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<VmNextRunConfigurationPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public VmNextRunConfigurationPopupView(EventBus eventBus) {
        super(eventBus, new VmNextRunConfigurationWidget(), "400px", "420px"); //$NON-NLS-1$ //$NON-NLS-2$
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }
}
