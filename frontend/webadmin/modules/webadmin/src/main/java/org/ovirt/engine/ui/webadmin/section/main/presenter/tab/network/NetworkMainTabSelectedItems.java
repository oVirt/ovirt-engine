package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network;

import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.ui.common.presenter.AbstractMainTabSelectedItems;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.NetworkSelectionChangeEvent;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class NetworkMainTabSelectedItems extends AbstractMainTabSelectedItems<NetworkView>
    implements NetworkSelectionChangeEvent.NetworkSelectionChangeHandler {

    @Inject
    NetworkMainTabSelectedItems(EventBus eventBus) {
        //This is singleton, so won't leak handlers.
        eventBus.addHandler(NetworkSelectionChangeEvent.getType(), this);
    }

    @Override
    public void onNetworkSelectionChange(NetworkSelectionChangeEvent event) {
        selectedItemsChanged(event.getSelectedItems());
    }
}
