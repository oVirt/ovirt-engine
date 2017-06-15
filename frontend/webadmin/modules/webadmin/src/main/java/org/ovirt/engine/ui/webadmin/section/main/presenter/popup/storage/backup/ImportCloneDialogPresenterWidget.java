package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.backup;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.storage.ImportCloneModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ImportCloneDialogPresenterWidget extends AbstractModelBoundPopupPresenterWidget<ImportCloneModel, ImportCloneDialogPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<ImportCloneModel> {
    }

    @Inject
    public ImportCloneDialogPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
