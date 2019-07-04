package org.ovirt.engine.core.bll.storage.disk.managedblock.util;

import java.util.List;

import javax.ejb.Singleton;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMap;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskImageDynamicDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.ImageStorageDomainMapDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmDao;

@Singleton
public class ManagedBlockStorageDiskUtil {

    @Inject
    private ImageStorageDomainMapDao imageStorageDomainMapDao;

    @Inject
    private DiskImageDynamicDao diskImageDynamicDao;

    @Inject
    private DiskImageDao diskImageDao;

    @Inject
    private SnapshotDao snapshotDao;

    @Inject
    private VmDao vmDao;

    @Inject
    private ImageDao imageDao;

    @Inject
    private ImagesHandler imagesHandler;

    public void saveDisk(ManagedBlockStorageDisk disk) {
        imageStorageDomainMapDao.save(new ImageStorageDomainMap(disk.getImageId(),
                disk.getStorageIds().get(0),
                disk.getQuotaId(),
                disk.getDiskProfileId()));

        DiskImageDynamic diskDynamic = new DiskImageDynamic();
        diskDynamic.setId(disk.getImageId());
        diskDynamic.setActualSize(disk.getActualSizeInBytes());
        diskImageDynamicDao.save(diskDynamic);
    }

    public void updateOldImageAsActive(Snapshot.SnapshotType snapshotType, boolean active, DiskImage diskImage) {
        Guid oldImageId = findImageForSameDrive(snapshotType, diskImage);

        DiskImage oldImage = diskImageDao.getSnapshotById(oldImageId);
        oldImage.setActive(active);
        imageDao.update(oldImage.getImage());
    }

    public void lockImage(Guid imageId) {
        imagesHandler.updateImageStatus(imageId, ImageStatus.LOCKED);
    }

    public void unlockImage(Guid imageId) {
        imagesHandler.updateImageStatus(imageId, ImageStatus.OK);
    }

    private Guid findImageForSameDrive(Snapshot.SnapshotType snapshotType, DiskImage diskImage) {
        List<VM> vmsListForDisk = vmDao.getVmsListForDisk(diskImage.getId(), false);
        Guid snapshotId = snapshotDao.getId(vmsListForDisk.get(0).getId(), snapshotType);
        return findImageForSameDrive(snapshotId, diskImage);
    }

    private Guid findImageForSameDrive(Guid snapshotId, DiskImage diskImage) {
        List<DiskImage> imagesFromSnapshot = diskImageDao.getAllSnapshotsForVmSnapshot(snapshotId);
        return imagesFromSnapshot.stream()
                .filter(currDiskImage -> diskImage.getId().equals(currDiskImage.getId()))
                .findFirst()
                .map(DiskImage::getImageId)
                .orElse(null);
    }
}
