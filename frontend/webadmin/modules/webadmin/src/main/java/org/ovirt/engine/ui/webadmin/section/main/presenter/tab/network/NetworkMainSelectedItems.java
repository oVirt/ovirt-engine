package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network;

import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.ui.common.presenter.AbstractMainSelectedItems;
import org.ovirt.engine.ui.webadmin.section.main.presenter.NetworkSelectionChangeEvent;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class NetworkMainSelectedItems extends AbstractMainSelectedItems<NetworkView>
    implements NetworkSelectionChangeEvent.NetworkSelectionChangeHandler {

    @Inject
    NetworkMainSelectedItems(EventBus eventBus) {
        //This is singleton, so won't leak handlers.
        eventBus.addHandler(NetworkSelectionChangeEvent.getType(), this);
    }

    @Override
    public void onNetworkSelectionChange(NetworkSelectionChangeEvent event) {
        selectedItemsChanged(event.getSelectedItems());
    }
}
