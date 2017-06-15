package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.storage.DisksAllocationModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class DisksAllocationPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<DisksAllocationModel, DisksAllocationPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<DisksAllocationModel> {
    }

    @Inject
    public DisksAllocationPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
