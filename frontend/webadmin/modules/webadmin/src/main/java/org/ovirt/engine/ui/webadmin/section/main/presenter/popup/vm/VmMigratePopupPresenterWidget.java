package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.MigrateModel;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VmMigratePopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<MigrateModel, VmMigratePopupPresenterWidget.ViewDef> {
    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<MigrateModel> {
    }

    @Inject
    public VmMigratePopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
