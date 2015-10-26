package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.disk;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.common.presenter.AbstractMainTabSelectedItems;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DiskSelectionChangeEvent;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class DiskMainTabSelectedItems extends AbstractMainTabSelectedItems<Disk>
    implements DiskSelectionChangeEvent.DiskSelectionChangeHandler {

    @Inject
    DiskMainTabSelectedItems(EventBus eventBus) {
        //This is singleton, so won't leak handlers.
        eventBus.addHandler(DiskSelectionChangeEvent.getType(), this);
    }

    @Override
    public void onDiskSelectionChange(DiskSelectionChangeEvent event) {
        selectedItemsChanged(event.getSelectedItems());
    }
}
