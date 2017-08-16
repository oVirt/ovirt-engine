package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.ui.common.presenter.AbstractMainSelectedItems;
import org.ovirt.engine.ui.webadmin.section.main.presenter.ClusterSelectionChangeEvent;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class ClusterMainSelectedItems extends AbstractMainSelectedItems<Cluster>
    implements ClusterSelectionChangeEvent.ClusterSelectionChangeHandler {

    @Inject
    ClusterMainSelectedItems(EventBus eventBus) {
        //This is singleton, so won't leak handlers.
        eventBus.addHandler(ClusterSelectionChangeEvent.getType(), this);
    }

    @Override
    public void onClusterSelectionChange(ClusterSelectionChangeEvent event) {
        selectedItemsChanged(event.getSelectedItems());
    }

}
