package org.ovirt.engine.ui.webadmin.widget.table.column;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.ui.common.widget.table.column.ColumnWithElementId;
import org.ovirt.engine.ui.common.widget.table.column.SortableColumn;

public class BrickStatusColumn extends SortableColumn<GlusterBrickEntity, GlusterBrickEntity> implements ColumnWithElementId {

    public BrickStatusColumn() {
        super(new BrickStatusCell());
    }

    @Override
    public GlusterBrickEntity getValue(GlusterBrickEntity object) {
        return object;
    }

    public void makeSortable() {
        makeSortable(new Comparator<GlusterBrickEntity>() {
            @Override
            public int compare(GlusterBrickEntity o1, GlusterBrickEntity o2) {
                return o1.getStatus().ordinal() - o2.getStatus().ordinal();
            }
        });
    }

    @Override
    public void configureElementId(String elementIdPrefix, String columnId) {
        getCell().setElementIdPrefix(elementIdPrefix);
        getCell().setColumnId(columnId);
    }

    @Override
    public BrickStatusCell getCell() {
        return (BrickStatusCell) super.getCell();
    }
}
