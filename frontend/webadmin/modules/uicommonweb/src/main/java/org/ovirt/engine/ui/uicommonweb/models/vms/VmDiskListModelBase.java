package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

public class VmDiskListModelBase extends SearchableListModel
{
    private EntityModel diskViewType;

    public EntityModel getDiskViewType() {
        return diskViewType;
    }

    public void setDiskViewType(EntityModel diskViewType) {
        this.diskViewType = diskViewType;
    }

    @Override
    protected String getListName() {
        return "VmDiskListModelBase"; //$NON-NLS-1$
    }

    public VmDiskListModelBase() {
        setDiskViewType(new EntityModel());
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setItems(Iterable value)
    {
        ArrayList<Disk> disks = value != null ? Linq.<Disk> cast(value) : new ArrayList<Disk>();
        ArrayList<Disk> filteredDisks = new ArrayList<Disk>();
        DiskStorageType diskStorageType = (DiskStorageType) getDiskViewType().getEntity();

        for (Disk disk : disks) {
            if (diskStorageType == null || diskStorageType == disk.getDiskStorageType()) {
                filteredDisks.add(disk);
            }
        }

        super.setItems(filteredDisks);
    }
}
