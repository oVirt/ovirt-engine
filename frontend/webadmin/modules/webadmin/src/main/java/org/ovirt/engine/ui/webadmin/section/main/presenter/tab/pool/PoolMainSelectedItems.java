package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.pool;

import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.ui.common.presenter.AbstractMainSelectedItems;
import org.ovirt.engine.ui.webadmin.section.main.presenter.PoolSelectionChangeEvent;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class PoolMainSelectedItems extends AbstractMainSelectedItems<VmPool>
    implements PoolSelectionChangeEvent.PoolSelectionChangeHandler {

    @Inject
    PoolMainSelectedItems(EventBus eventBus) {
        //This is singleton, so won't leak handlers.
        eventBus.addHandler(PoolSelectionChangeEvent.getType(), this);
    }

    @Override
    public void onPoolSelectionChange(PoolSelectionChangeEvent event) {
        selectedItemsChanged(event.getSelectedItems());
    }
}
