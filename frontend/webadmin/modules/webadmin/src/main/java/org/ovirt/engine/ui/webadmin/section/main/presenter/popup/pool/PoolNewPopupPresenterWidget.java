package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.pool;

import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AbstractModelBoundPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class PoolNewPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<UnitVmModel, PoolNewPopupPresenterWidget.ViewDef> {
    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<UnitVmModel> {

    }

    @Inject
    public PoolNewPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
