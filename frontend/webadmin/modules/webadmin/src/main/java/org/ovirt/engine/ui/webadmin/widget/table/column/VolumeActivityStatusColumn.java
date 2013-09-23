package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;

import com.google.gwt.user.cellview.client.Column;

public class VolumeActivityStatusColumn extends Column<GlusterVolumeEntity, GlusterVolumeEntity> {

    public VolumeActivityStatusColumn() {
        super(new VolumeActivityStatusCell());
    }

    @Override
    public GlusterVolumeEntity getValue(GlusterVolumeEntity object) {
        return object;
    }

}
