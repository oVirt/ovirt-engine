package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.common.widget.table.column.ColumnWithElementId;
import org.ovirt.engine.ui.common.widget.table.column.SortableColumn;

public class VolumeStatusColumn extends SortableColumn<GlusterVolumeEntity, GlusterVolumeEntity> implements ColumnWithElementId {

    public VolumeStatusColumn() {
        super(new VolumeStatusCell());
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
    public VolumeStatusCell getCell() {
        return (VolumeStatusCell) super.getCell();
    }
}
