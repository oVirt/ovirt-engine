package org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.AbstractDiskModel;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VmDiskPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<AbstractDiskModel, VmDiskPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<AbstractDiskModel> {
        boolean handleEnterKeyDisabled();
    }

    @Inject
    public VmDiskPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    protected void handleEnterKey() {
        if (!getView().handleEnterKeyDisabled()) {
            super.handleEnterKey();
        }
    }
}
