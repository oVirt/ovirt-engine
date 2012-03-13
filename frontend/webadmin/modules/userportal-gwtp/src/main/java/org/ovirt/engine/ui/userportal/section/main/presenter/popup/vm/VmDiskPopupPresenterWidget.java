package org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VmDiskPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<DiskModel, VmDiskPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<DiskModel> {
    }

    @Inject
    public VmDiskPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
