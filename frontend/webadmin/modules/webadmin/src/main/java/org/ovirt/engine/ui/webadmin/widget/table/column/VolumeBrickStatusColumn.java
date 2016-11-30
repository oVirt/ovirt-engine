package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.webadmin.widget.table.cell.VolumeBrickStatusCell;

public class VolumeBrickStatusColumn extends AbstractColumn<GlusterVolumeEntity, GlusterVolumeEntity> {

    public VolumeBrickStatusColumn() {
        super(new VolumeBrickStatusCell());
    }

    @Override
    public GlusterVolumeEntity getValue(GlusterVolumeEntity object) {
        return object;
    }

    @Override
    public String getTooltip(GlusterVolumeEntity object) {
        String status = EnumTranslator.getInstance().translate(object.getStatus());
        return status;
    }

}
