package org.ovirt.engine.ui.webadmin.section.main.view.popup.ova;

import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundWidgetPopupView;
import org.ovirt.engine.ui.common.widget.uicommon.popup.ova.ExportOvaWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.ExportOvaModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.ova.ExportOvaPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ExportOvaPopupView extends AbstractModelBoundWidgetPopupView<ExportOvaModel> implements ExportOvaPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<ExportOvaModel, ExportOvaPopupView> {
    }

    interface ViewIdHandler extends ElementIdHandler<ExportOvaPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public ExportOvaPopupView(EventBus eventBus) {
        super(eventBus, new ExportOvaWidget(), "500px", "330px"); //$NON-NLS-1$ //$NON-NLS-2$
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}
