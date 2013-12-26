package org.ovirt.engine.ui.userportal.section.main.view.popup.vm;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundWidgetPopupView;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.VmSnapshotCustomPreviewPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.PreviewSnapshotModel;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationMessages;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.ApplicationTemplates;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmSnapshotCustomPreviewPopupPresenterWidget;

public class VmSnapshotCustomPreviewPopupView extends AbstractModelBoundWidgetPopupView<PreviewSnapshotModel> implements VmSnapshotCustomPreviewPopupPresenterWidget.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<VmSnapshotCustomPreviewPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public VmSnapshotCustomPreviewPopupView(EventBus eventBus,
                                            ApplicationResources resources,
                                            ApplicationConstants constants,
                                            ApplicationMessages messages,
                                            ApplicationTemplates templates) {
        super(eventBus, resources, new VmSnapshotCustomPreviewPopupWidget(resources, constants, messages, templates), "900px", "600px"); //$NON-NLS-1$ //$NON-NLS-2$
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }
}
