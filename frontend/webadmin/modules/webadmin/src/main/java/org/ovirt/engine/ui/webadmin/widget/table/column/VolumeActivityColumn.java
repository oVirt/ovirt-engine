package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;

import com.google.gwt.user.cellview.client.Column;

public class VolumeActivityColumn extends Column<GlusterVolumeEntity, GlusterVolumeEntity> {

    public VolumeActivityColumn() {
        super(new VolumeActivityCell());
    }

    @Override
    public GlusterVolumeEntity getValue(GlusterVolumeEntity object) {
        return object;
    }

}
