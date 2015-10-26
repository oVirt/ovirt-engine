package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.common.presenter.AbstractMainTabSelectedItems;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.VolumeSelectionChangeEvent;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class VolumeMainTabSelectedItems extends AbstractMainTabSelectedItems<GlusterVolumeEntity>
    implements VolumeSelectionChangeEvent.VolumeSelectionChangeHandler {

    @Inject
    VolumeMainTabSelectedItems(EventBus eventBus) {
        //This is singleton, so won't leak handlers.
        eventBus.addHandler(VolumeSelectionChangeEvent.getType(), this);
    }

    @Override
    public void onVolumeSelectionChange(VolumeSelectionChangeEvent event) {
        selectedItemsChanged(event.getSelectedItems());
    }
}
