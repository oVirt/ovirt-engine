package org.ovirt.engine.ui.uicommonweb.models.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

public class VolumeBrickListModel extends SearchableListModel {

    @Override
    protected String getListName() {
        return "VolumeBrickListModel";
    }

    public VolumeBrickListModel() {
        setIsTimerDisabled(false);
    }

    @Override
    protected void OnEntityChanged() {
        super.OnEntityChanged();
        if (getEntity() == null) {
            return;
        }
        GlusterVolumeEntity glusterVolumeEntity = (GlusterVolumeEntity) getEntity();
        setItems(glusterVolumeEntity.getBricks());
    }

    @Override
    protected void SyncSearch() {
        OnEntityChanged();
    }

}
