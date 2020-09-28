package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.DiskSnapshot;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class DiskSnapshotMapper {

    @Mapping(from = org.ovirt.engine.core.common.businessentities.storage.Disk.class, to = DiskSnapshot.class)
    public static DiskSnapshot map(org.ovirt.engine.core.common.businessentities.storage.Disk entity, DiskSnapshot template) {
        if (template == null) {
            template = new DiskSnapshot();
        }
        DiskSnapshot model = (DiskSnapshot) DiskMapper.map(entity, template);

        Disk disk = new Disk();
        disk.setId(entity.getId().toString());
        model.setDisk(disk);

        DiskImage diskImage = (DiskImage) entity;
        model.setId(diskImage.getImageId().toString());
        model.setImageId(null);

        if (!Guid.isNullOrEmpty(diskImage.getParentId())) {
            DiskSnapshot parent = new DiskSnapshot();
            parent.setId(diskImage.getParentId().toString());

            // Add storage domain to allow creating a link later.
            StorageDomain storageDomain = new StorageDomain();
            storageDomain.setId(diskImage.getStorageIds().get(0).toString());
            parent.setStorageDomain(storageDomain);

            model.setParent(parent);
        }

        return model;
    }

    @Mapping(from = DiskSnapshot.class, to = org.ovirt.engine.core.common.businessentities.storage.Disk.class)
    public static org.ovirt.engine.core.common.businessentities.storage.Disk map(DiskSnapshot diskSnapshot, org.ovirt.engine.core.common.businessentities.storage.Disk template) {
        DiskImage engineDisk = (DiskImage) DiskMapper.map(diskSnapshot, template);

        engineDisk.setImageId(GuidUtils.asGuid(diskSnapshot.getId()));

        if (diskSnapshot.isSetDisk()) {
            engineDisk.setId(GuidUtils.asGuid(diskSnapshot.getDisk().getId()));
        }

        return engineDisk;
    }


}
