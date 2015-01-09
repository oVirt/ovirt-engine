package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.webadmin.widget.table.cell.VolumeBrickStatusCell;

import com.google.gwt.user.cellview.client.Column;

public class VolumeBrickStatusColumn extends Column<GlusterVolumeEntity, GlusterVolumeEntity> {

    public VolumeBrickStatusColumn() {
        super(new VolumeBrickStatusCell());
    }

    @Override
    public GlusterVolumeEntity getValue(GlusterVolumeEntity object) {
        return object;
    }

}
