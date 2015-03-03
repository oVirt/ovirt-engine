package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.webadmin.widget.table.cell.VolumeInfoCell;

public class VolumeInfoColumn extends AbstractColumn<GlusterVolumeEntity, GlusterVolumeEntity> {

    public VolumeInfoColumn() {
        super(new VolumeInfoCell());
    }

    @Override
    public GlusterVolumeEntity getValue(GlusterVolumeEntity object) {
        return object;
    }
}
