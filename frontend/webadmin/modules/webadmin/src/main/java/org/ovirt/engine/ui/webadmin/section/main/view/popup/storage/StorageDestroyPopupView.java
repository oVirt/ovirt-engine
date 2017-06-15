package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import org.ovirt.engine.ui.common.view.popup.ForceRemoveConfirmationPopupView;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StorageDestroyPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class StorageDestroyPopupView extends ForceRemoveConfirmationPopupView
        implements StorageDestroyPopupPresenterWidget.ViewDef {


    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public StorageDestroyPopupView(EventBus eventBus) {
        super(eventBus);
    }

    @Override
    protected String getWarning() {
        return constants.storageDestroyPopupWarningLabel();
    }

    @Override
    protected String getFormattedMessage(String itemName) {
        return messages.storageDestroyPopupMessageLabel(itemName);
    }

}
