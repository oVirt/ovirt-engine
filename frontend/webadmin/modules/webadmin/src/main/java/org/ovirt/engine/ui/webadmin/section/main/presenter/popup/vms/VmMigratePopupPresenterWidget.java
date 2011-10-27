package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vms;

import org.ovirt.engine.ui.uicommonweb.models.vms.VmModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AbstractModelBoundPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VmMigratePopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<VmModel, VmMigratePopupPresenterWidget.ViewDef> {
    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<VmModel> {
    }

    @Inject
    public VmMigratePopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
