package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.PreviewSnapshotModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VmSnapshotCustomPreviewPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<PreviewSnapshotModel, VmSnapshotCustomPreviewPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<PreviewSnapshotModel> {
    }

    @Inject
    public VmSnapshotCustomPreviewPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
