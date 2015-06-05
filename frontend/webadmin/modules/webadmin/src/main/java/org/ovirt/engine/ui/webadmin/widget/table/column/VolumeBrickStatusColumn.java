package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.common.widget.table.column.ColumnWithElementId;

import com.google.gwt.user.cellview.client.Column;

public class VolumeBrickStatusColumn extends Column<GlusterVolumeEntity, GlusterVolumeEntity> implements ColumnWithElementId {

    public VolumeBrickStatusColumn() {
        super(new VolumeBrickStatusCell());
    }

    @Override
    public GlusterVolumeEntity getValue(GlusterVolumeEntity object) {
        return object;
    }

    @Override
    public void configureElementId(String elementIdPrefix, String columnId) {
        getCell().setElementIdPrefix(elementIdPrefix);
        getCell().setColumnId(columnId);
    }

    @Override
    public VolumeBrickStatusCell getCell() {
        return (VolumeBrickStatusCell) super.getCell();
    }
}
