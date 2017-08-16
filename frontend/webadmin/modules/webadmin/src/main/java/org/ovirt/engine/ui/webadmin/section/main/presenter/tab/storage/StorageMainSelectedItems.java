package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.ui.common.presenter.AbstractMainSelectedItems;
import org.ovirt.engine.ui.webadmin.section.main.presenter.StorageSelectionChangeEvent;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class StorageMainSelectedItems extends AbstractMainSelectedItems<StorageDomain>
    implements StorageSelectionChangeEvent.StorageSelectionChangeHandler {

    @Inject
    StorageMainSelectedItems(EventBus eventBus) {
        //This is singleton, so won't leak handlers.
        eventBus.addHandler(StorageSelectionChangeEvent.getType(), this);
    }

    @Override
    public void onStorageSelectionChange(StorageSelectionChangeEvent event) {
        selectedItemsChanged(event.getSelectedItems());
    }
}
