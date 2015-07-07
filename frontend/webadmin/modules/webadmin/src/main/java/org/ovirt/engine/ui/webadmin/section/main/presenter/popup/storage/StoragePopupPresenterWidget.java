package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageModel;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class StoragePopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<StorageModel, StoragePopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<StorageModel> {
        boolean handleEnterKeyDisabled();
    }

    @Inject
    public StoragePopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    protected void handleEnterKey() {
        if (!getView().handleEnterKeyDisabled()) {
            super.handleEnterKey();
        }
    }

}
