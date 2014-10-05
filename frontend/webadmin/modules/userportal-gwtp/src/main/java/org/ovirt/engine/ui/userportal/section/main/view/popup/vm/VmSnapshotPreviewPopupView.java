package org.ovirt.engine.ui.userportal.section.main.view.popup.vm;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundWidgetPopupView;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.VmSnapshotPreviewPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.SnapshotModel;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationMessages;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmSnapshotPreviewPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VmSnapshotPreviewPopupView extends AbstractModelBoundWidgetPopupView<SnapshotModel> implements VmSnapshotPreviewPopupPresenterWidget.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<VmSnapshotPreviewPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public VmSnapshotPreviewPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants, ApplicationMessages messages) {
        super(eventBus, resources, new VmSnapshotPreviewPopupWidget(constants, messages), "750px", "450px"); //$NON-NLS-1$ //$NON-NLS-2$
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }
}
