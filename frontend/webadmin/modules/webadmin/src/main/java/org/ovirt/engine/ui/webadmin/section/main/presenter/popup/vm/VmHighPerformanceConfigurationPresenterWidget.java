package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmHighPerformanceConfigurationModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VmHighPerformanceConfigurationPresenterWidget extends AbstractModelBoundPopupPresenterWidget<VmHighPerformanceConfigurationModel, VmHighPerformanceConfigurationPresenterWidget.ViewDef> {
    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<VmHighPerformanceConfigurationModel> {
    }

    @Inject
    public VmHighPerformanceConfigurationPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
