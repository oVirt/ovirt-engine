package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;

import com.google.gwt.user.cellview.client.Column;

public class BrickStatusColumn extends Column<GlusterBrickEntity, GlusterBrickEntity> {

    public BrickStatusColumn() {
        super(new BrickStatusCell());
    }

    @Override
    public GlusterBrickEntity getValue(GlusterBrickEntity object) {
        return object;
    }

}
