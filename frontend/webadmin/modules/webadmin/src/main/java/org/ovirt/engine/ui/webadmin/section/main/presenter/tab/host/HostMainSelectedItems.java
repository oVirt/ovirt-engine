package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.presenter.AbstractMainSelectedItems;
import org.ovirt.engine.ui.webadmin.section.main.presenter.HostSelectionChangeEvent;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class HostMainSelectedItems extends AbstractMainSelectedItems<VDS> implements HostSelectionChangeEvent.HostSelectionChangeHandler {

    @Inject
    HostMainSelectedItems(EventBus eventBus) {
        //This is singleton, so won't leak handlers.
        eventBus.addHandler(HostSelectionChangeEvent.getType(), this);
    }

    @Override
    public void onHostSelectionChange(HostSelectionChangeEvent event) {
        selectedItemsChanged(event.getSelectedItems());
    }
}
