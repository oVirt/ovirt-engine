package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.backup;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmFromExportDomainModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ImportVmFromExportDomainPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<ImportVmFromExportDomainModel, ImportVmFromExportDomainPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<ImportVmFromExportDomainModel> {
    }

    @Inject
    public ImportVmFromExportDomainPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
