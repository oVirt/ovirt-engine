package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.businessentities.DiskImage;

import com.google.gwt.user.cellview.client.Column;

public class DiskStatusColumn extends Column<DiskImage, DiskImage> {

    public DiskStatusColumn() {
        super(new DiskStatusCell());
    }

    @Override
    public DiskImage getValue(DiskImage object) {
        return object;
    }

}
