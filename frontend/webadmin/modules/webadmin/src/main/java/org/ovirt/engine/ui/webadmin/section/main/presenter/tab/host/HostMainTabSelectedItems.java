package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.presenter.AbstractMainTabSelectedItems;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.HostSelectionChangeEvent;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class HostMainTabSelectedItems extends AbstractMainTabSelectedItems<VDS> implements HostSelectionChangeEvent.HostSelectionChangeHandler {

    @Inject
    HostMainTabSelectedItems(EventBus eventBus) {
        //This is singleton, so won't leak handlers.
        eventBus.addHandler(HostSelectionChangeEvent.getType(), this);
    }

    @Override
    public void onHostSelectionChange(HostSelectionChangeEvent event) {
        selectedItemsChanged(event.getSelectedItems());
    }
}
