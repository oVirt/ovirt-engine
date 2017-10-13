package org.ovirt.engine.ui.webadmin.section.main.view.popup.vm;

import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundWidgetPopupView;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.VmExportOvaWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.ExportOvaModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmExportOvaPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VmExportOvaPopupView extends AbstractModelBoundWidgetPopupView<ExportOvaModel> implements VmExportOvaPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<ExportOvaModel, VmExportOvaPopupView> {
    }

    interface ViewIdHandler extends ElementIdHandler<VmExportOvaPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public VmExportOvaPopupView(EventBus eventBus) {
        super(eventBus, new VmExportOvaWidget(), "500px", "330px"); //$NON-NLS-1$ //$NON-NLS-2$
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}
