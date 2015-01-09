package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterTaskSupport;
import org.ovirt.engine.ui.webadmin.widget.table.cell.VolumeActivityStatusCell;

import com.google.gwt.user.cellview.client.Column;

public class VolumeActivityStatusColumn<T extends GlusterTaskSupport> extends Column<T, GlusterTaskSupport> {

    public VolumeActivityStatusColumn() {
        super(new VolumeActivityStatusCell<GlusterTaskSupport>());
    }

    @Override
    public GlusterTaskSupport getValue(T object) {
        return object;
    }

}
