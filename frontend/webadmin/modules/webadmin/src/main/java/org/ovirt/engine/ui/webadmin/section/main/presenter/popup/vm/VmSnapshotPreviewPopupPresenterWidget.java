package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.SnapshotModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VmSnapshotPreviewPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<SnapshotModel, VmSnapshotPreviewPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<SnapshotModel> {
    }

    @Inject
    public VmSnapshotPreviewPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
