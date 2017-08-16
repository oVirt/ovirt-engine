package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.common.presenter.AbstractMainSelectedItems;
import org.ovirt.engine.ui.webadmin.section.main.presenter.VolumeSelectionChangeEvent;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class VolumeMainSelectedItems extends AbstractMainSelectedItems<GlusterVolumeEntity>
    implements VolumeSelectionChangeEvent.VolumeSelectionChangeHandler {

    @Inject
    VolumeMainSelectedItems(EventBus eventBus) {
        //This is singleton, so won't leak handlers.
        eventBus.addHandler(VolumeSelectionChangeEvent.getType(), this);
    }

    @Override
    public void onVolumeSelectionChange(VolumeSelectionChangeEvent event) {
        selectedItemsChanged(event.getSelectedItems());
    }
}
