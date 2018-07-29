package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.ova;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.ExportOvaModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ExportOvaPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<ExportOvaModel, ExportOvaPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<ExportOvaModel> {
    }

    @Inject
    public ExportOvaPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
