package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.ui.common.widget.table.column.AbstractSortableColumn;
import org.ovirt.engine.ui.uicommonweb.Linq;

public class StorageDeviceStatusColumn extends AbstractSortableColumn<StorageDevice, StorageDevice> {

    public StorageDeviceStatusColumn() {
        super(new StorageDeviceStatusCell());
    }

    @Override
    public StorageDevice getValue(StorageDevice object) {
        return object;
    }

    public void makeSortable() {
        makeSortable(new Linq.StorageDeviceComparer());
    }
}
