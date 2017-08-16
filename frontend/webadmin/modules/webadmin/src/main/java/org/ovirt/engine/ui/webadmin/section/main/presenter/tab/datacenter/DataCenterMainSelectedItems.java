package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.common.presenter.AbstractMainSelectedItems;
import org.ovirt.engine.ui.webadmin.section.main.presenter.DataCenterSelectionChangeEvent;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class DataCenterMainSelectedItems extends AbstractMainSelectedItems<StoragePool>
    implements DataCenterSelectionChangeEvent.DataCenterSelectionChangeHandler {

    @Inject
    DataCenterMainSelectedItems(EventBus eventBus) {
        //This is singleton, so won't leak handlers.
        eventBus.addHandler(DataCenterSelectionChangeEvent.getType(), this);
    }

    @Override
    public void onDataCenterSelectionChange(DataCenterSelectionChangeEvent event) {
        selectedItemsChanged(event.getSelectedItems());
    }

}
