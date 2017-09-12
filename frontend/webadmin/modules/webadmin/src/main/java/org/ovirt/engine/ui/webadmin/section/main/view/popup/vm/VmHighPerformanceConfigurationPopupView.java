package org.ovirt.engine.ui.webadmin.section.main.view.popup.vm;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundWidgetPopupView;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.VmHighPerformanceConfigurationWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmHighPerformanceConfigurationModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmHighPerformanceConfigurationPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;


public class VmHighPerformanceConfigurationPopupView extends AbstractModelBoundWidgetPopupView<VmHighPerformanceConfigurationModel>
        implements VmHighPerformanceConfigurationPresenterWidget.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<VmHighPerformanceConfigurationPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public VmHighPerformanceConfigurationPopupView(EventBus eventBus) {
        super(eventBus, new VmHighPerformanceConfigurationWidget(), "750px", "650px"); //$NON-NLS-1$ //$NON-NLS-2$
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }
}
