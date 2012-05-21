package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.backup;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ImportVmPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<ImportVmModel, ImportVmPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<ImportVmModel> {
    }

    @Inject
    public ImportVmPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
