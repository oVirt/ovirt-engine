package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.hostdev;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.hostdev.RepinHostModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VmRepinHostPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<RepinHostModel, VmRepinHostPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<RepinHostModel> {
    }

    @Inject
    public VmRepinHostPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
