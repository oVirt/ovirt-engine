package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.businessentities.Disk;

import com.google.gwt.user.cellview.client.Column;

public class DiskStatusColumn extends Column<Disk, Disk> {

    public DiskStatusColumn() {
        super(new DiskStatusCell());
    }

    @Override
    public Disk getValue(Disk object) {
        return object;
    }

}
