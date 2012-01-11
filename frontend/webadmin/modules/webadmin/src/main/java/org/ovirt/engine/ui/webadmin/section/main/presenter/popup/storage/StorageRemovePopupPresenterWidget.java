package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.storage.RemoveStorageModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class StorageRemovePopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<RemoveStorageModel, StorageRemovePopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<RemoveStorageModel> {
    }

    @Inject
    public StorageRemovePopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
