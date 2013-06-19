package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.storage.ImportExportRepoImageBaseModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ImportExportImagePopupPresenterWidget extends
        AbstractModelBoundPopupPresenterWidget<ImportExportRepoImageBaseModel, ImportExportImagePopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<ImportExportRepoImageBaseModel> {
    }

    @Inject
    public ImportExportImagePopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
