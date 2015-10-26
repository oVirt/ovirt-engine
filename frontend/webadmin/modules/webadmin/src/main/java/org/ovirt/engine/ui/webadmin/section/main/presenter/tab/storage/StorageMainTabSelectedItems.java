package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.ui.common.presenter.AbstractMainTabSelectedItems;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.StorageSelectionChangeEvent;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class StorageMainTabSelectedItems extends AbstractMainTabSelectedItems<StorageDomain>
    implements StorageSelectionChangeEvent.StorageSelectionChangeHandler {

    @Inject
    StorageMainTabSelectedItems(EventBus eventBus) {
        //This is singleton, so won't leak handlers.
        eventBus.addHandler(StorageSelectionChangeEvent.getType(), this);
    }

    @Override
    public void onStorageSelectionChange(StorageSelectionChangeEvent event) {
        selectedItemsChanged(event.getSelectedItems());
    }
}
