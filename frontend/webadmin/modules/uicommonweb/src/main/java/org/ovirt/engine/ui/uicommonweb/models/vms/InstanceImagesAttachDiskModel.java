package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

public class InstanceImagesAttachDiskModel extends AttachDiskModel {

    public void loadAttachableDisks(int os, Version compatibilityVersion, final Disk prevSelectedDisk) {
        // Get image attachable disks
        AsyncDataProvider.getInstance().getAllAttachableDisks(
                new AsyncQuery<>(new InstanceImageGetDisksCallback(DiskStorageType.IMAGE, prevSelectedDisk)
                ), getVm().getStoragePoolId(), getVm().getId());

        // Get lun attachable disks
        AsyncDataProvider.getInstance().getAllAttachableDisks(
                new AsyncQuery<>(new InstanceImageGetDisksCallback(DiskStorageType.LUN, prevSelectedDisk)
                ), null, getVm().getId());

        // Get managed block attachable disks
        AsyncDataProvider.getInstance().getAllAttachableDisks(
                new AsyncQuery<>(new InstanceImageGetDisksCallback(DiskStorageType.MANAGED_BLOCK_STORAGE, prevSelectedDisk)
                ), getVm().getStoragePoolId(), getVm().getId());
    }

    public void loadAttachableDisks(Disk prevSelected) {
        doLoadAttachableDisks(new InstanceImageGetDisksCallback(DiskStorageType.IMAGE, prevSelected),
                new InstanceImageGetDisksCallback(DiskStorageType.LUN, prevSelected),
                new InstanceImageGetDisksCallback(DiskStorageType.MANAGED_BLOCK_STORAGE, prevSelected));
    }

    class InstanceImageGetDisksCallback extends GetDisksCallback {

        private DiskStorageType diskStorageType;

        private final Disk prevSelectedDisk;

        InstanceImageGetDisksCallback(DiskStorageType diskStorageType, Disk prevSelectedDisk) {
            super(diskStorageType);
            this.diskStorageType = diskStorageType;
            this.prevSelectedDisk = prevSelectedDisk;
        }

        @Override
        protected void initAttachableDisks(List<EntityModel<DiskModel>> entities) {
            getAttachableDisksMap().get(diskStorageType).setItems(entities, selectedOrNull(entities));
        }

        private EntityModel<DiskModel> selectedOrNull(List<EntityModel<DiskModel>> list) {
            if (prevSelectedDisk == null) {
                return null;
            }

            if (prevSelectedDisk.getDiskStorageType() != diskStorageType) {
                return null;
            }

            for (EntityModel<DiskModel> item : list) {
                if (item.getEntity().getDisk().getId().equals(prevSelectedDisk.getId())) {
                    return item;
                }
            }

            return null;
        }

        @Override
        protected List<Disk> adjustReturnValue(List<Disk> disksFromServer) {
            List<Guid> inDialogIds = asIds(getAttachedNotSubmittedDisks());

            List<Disk> res = new ArrayList<>();

            for (Disk diskFromServer : disksFromServer) {
                boolean selectedDisk = prevSelectedDisk != null &&
                        diskFromServer.getId().equals(prevSelectedDisk.getId()) &&
                        prevSelectedDisk.getDiskStorageType() != diskStorageType;

                if (!selectedDisk) {
                    if (!inDialogIds.contains(diskFromServer.getId())) {
                        res.add(diskFromServer);
                    }
                } else {
                    res.add(prevSelectedDisk);
                }
            }

            return res;
        }

        private List<Guid> asIds(List<Disk> attachedNotSubmittedDisks) {
            List<Guid> res = new ArrayList<>();

            for (Disk disk : attachedNotSubmittedDisks) {
                res.add(disk.getId());
            }

            return res;
        }
    }

    protected List<Disk> getAttachedNotSubmittedDisks() {
        return Collections.emptyList();
    }
}
