package org.ovirt.engine.ui.webadmin.widget.table.column;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.webadmin.widget.table.cell.BrickStatusCell;

public class BrickStatusColumn extends AbstractColumn<GlusterBrickEntity, GlusterBrickEntity> {

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
}
