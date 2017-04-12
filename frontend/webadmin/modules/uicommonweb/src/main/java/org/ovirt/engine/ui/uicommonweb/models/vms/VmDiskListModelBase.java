package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Collection;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

public class VmDiskListModelBase<E> extends SearchableListModel<E, Disk> {
    private EntityModel<DiskStorageType> diskViewType;

    public EntityModel<DiskStorageType> getDiskViewType() {
        return diskViewType;
    }

    public void setDiskViewType(EntityModel<DiskStorageType> diskViewType) {
        this.diskViewType = diskViewType;
    }

    @Override
    protected String getListName() {
        return "VmDiskListModelBase"; //$NON-NLS-1$
    }

    public VmDiskListModelBase() {
        setDiskViewType(new EntityModel<DiskStorageType>());
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setItems(Collection<Disk> value) {
        Collection<Disk> disks = value != null ? value : new ArrayList<Disk>();
        ArrayList<Disk> filteredDisks = new ArrayList<>();
        DiskStorageType diskStorageType = getDiskViewType().getEntity();

        for (Disk disk : disks) {
            if (diskStorageType == null || diskStorageType == disk.getDiskStorageType()) {
                filteredDisks.add(disk);
            }
        }

        super.setItems(filteredDisks);
    }
}
