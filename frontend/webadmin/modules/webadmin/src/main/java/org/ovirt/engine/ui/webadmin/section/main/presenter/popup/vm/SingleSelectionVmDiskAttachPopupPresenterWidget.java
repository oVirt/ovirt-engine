package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.AttachDiskModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SingleSelectionVmDiskAttachPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<AttachDiskModel, SingleSelectionVmDiskAttachPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<AttachDiskModel> {
    }

    @Inject
    public SingleSelectionVmDiskAttachPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
