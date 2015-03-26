package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

import java.util.ArrayList;
import java.util.List;

public class InstanceImagesAttachDiskModel extends AttachDiskModel {

    public void loadAttachableDisks(int os, Version compatibilityVersion, final Disk prevSelectedDisk) {
        // Get image attachable disks
        AsyncDataProvider.getInstance().getFilteredAttachableDisks(
                new AsyncQuery(this, new InstanceImageGetDisksCallback(DiskStorageType.IMAGE, prevSelectedDisk)
                ), getVm().getStoragePoolId(), getVm().getId(), os, compatibilityVersion);

        // Get lun attachable disks
        AsyncDataProvider.getInstance().getFilteredAttachableDisks(
                new AsyncQuery(this, new InstanceImageGetDisksCallback(DiskStorageType.LUN, prevSelectedDisk)
                ), null, getVm().getId(), os, compatibilityVersion);

        // Get cinder attachable disks
        AsyncDataProvider.getInstance().getFilteredAttachableDisks(
                new AsyncQuery(this, new InstanceImageGetDisksCallback(DiskStorageType.CINDER, prevSelectedDisk)
                ), null, getVm().getId(), os, compatibilityVersion);
    }

    public void loadAttachableDisks(Disk prevSelected) {
        doLoadAttachableDisks(new InstanceImageGetDisksCallback(DiskStorageType.IMAGE, prevSelected),
                new InstanceImageGetDisksCallback(DiskStorageType.LUN, prevSelected),
                new InstanceImageGetDisksCallback(DiskStorageType.CINDER, prevSelected));
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
        protected List<Disk> adjustReturnValue(Object returnValue) {
            List<Disk> disksFromServer = (List<Disk>) returnValue;

            if (prevSelectedDisk == null) {
                return disksFromServer;
            }

            if (prevSelectedDisk.getDiskStorageType() != diskStorageType) {
                return disksFromServer;
            }

            List<Disk> res = new ArrayList<>();

            for (Disk diskFromServer : disksFromServer) {
                if (!diskFromServer.getId().equals(prevSelectedDisk.getId())) {
                    res.add(diskFromServer);
                } else {
                    res.add(prevSelectedDisk);
                }
            }

            return res;
        }
    }


}
