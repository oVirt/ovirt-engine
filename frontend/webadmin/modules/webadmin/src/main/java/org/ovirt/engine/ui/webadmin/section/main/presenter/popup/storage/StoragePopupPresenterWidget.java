package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class StoragePopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<StorageModel, StoragePopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<StorageModel> {
        boolean handleEnterKeyDisabled();

        void focusDiscardAfterDelete();
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

    @SuppressWarnings("unchecked")
    @Override
    public void init(final StorageModel model) {
        super.init(model);

        model.getDiscardAfterDelete().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            EntityModel<Boolean> discardAfterDelete = (EntityModel<Boolean>) sender;
            String propertyName = args.propertyName;

            if ("IsValid".equals(propertyName) && !discardAfterDelete.getIsValid()) { //$NON-NLS-1$
                getView().focusDiscardAfterDelete();
            }
        });
    }

}
