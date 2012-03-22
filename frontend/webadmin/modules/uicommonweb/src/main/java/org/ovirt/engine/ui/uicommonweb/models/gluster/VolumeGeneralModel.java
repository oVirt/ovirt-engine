package org.ovirt.engine.ui.uicommonweb.models.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

public class VolumeGeneralModel extends EntityModel {
    @Override
    protected void OnEntityChanged() {
        super.OnEntityChanged();
        updatePropeties();
    }

    private void updatePropeties() {
        GlusterVolumeEntity entity = (GlusterVolumeEntity) getEntity();
    }

}
