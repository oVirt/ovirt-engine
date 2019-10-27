package org.ovirt.engine.core.bll.kubevirt;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskImageDynamicDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.ImageStorageDomainMapDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

import io.kubernetes.client.models.V1PersistentVolumeClaim;

@ApplicationScoped
public class DiskUpdater {

    @Inject
    private DiskImageDao diskImageDao;

    @Inject
    private ImageDao imageDao;

    @Inject
    private ImageStorageDomainMapDao imageStorageDomainMapDao;

    @Inject
    private DiskImageDynamicDao diskImageDynamicDao;

    @Inject
    private BaseDiskDao baseDiskDao;

    @Inject
    private Instance<BackendInternal> backend;

    public boolean addDisk(V1PersistentVolumeClaim pvc, Guid clusterId) {
        if (diskImageDao.getAllForStorageDomain(clusterId).stream().map(PVCDisk::new).anyMatch(equals(pvc))) {
            return false;
        }
        ActionReturnValue retVal = backend.get()
                .runInternalAction(
                        ActionType.AddDisk,
                        buildAddDiskParameters(pvc, clusterId));
        return retVal.getSucceeded();
    }

    private AddDiskParameters buildAddDiskParameters(V1PersistentVolumeClaim pvc, Guid clusterId) {
        PVCDisk pvcDisk = new PVCDisk();
        pvcDisk.setName(pvc.getMetadata().getName());
        pvcDisk.setNamespace(pvc.getMetadata().getNamespace());
        AddDiskParameters parameters = new AddDiskParameters(pvcDisk.toDisk());
        parameters.setStorageDomainId(clusterId);
        return parameters;
    }

    public boolean removeDisk(V1PersistentVolumeClaim pvc, Guid clusterId) {
        List<DiskImage> disks = diskImageDao.getAllForStorageDomain(clusterId);
        Optional<PVCDisk> disk = disks.stream().map(PVCDisk::new).filter(equals(pvc)).findFirst();
        return disk.isPresent() ? removeFromDB(disk.get()) : false;
    }

    public static Predicate<PVCDisk> equals(V1PersistentVolumeClaim pvc) {
        return disk -> equals(pvc, disk);
    }

    public static Predicate<V1PersistentVolumeClaim> equals(PVCDisk disk) {
        return pvc -> equals(pvc, disk);
    }

    private static boolean equals(V1PersistentVolumeClaim pvc, PVCDisk disk) {
        return pvc.getMetadata().getName().equals(disk.getName()) &&
                pvc.getMetadata().getNamespace().equals(disk.getNamespace());
    }

    public boolean removeFromDB(PVCDisk pvcDisk) {
        DiskImage disk = pvcDisk.toDisk();
        // no snapshots in kubevirt
        TransactionSupport.executeInNewTransaction(() -> {
            baseDiskDao.remove(disk.getId());
            imageStorageDomainMapDao.remove(disk.getImageId());
            imageDao.remove(disk.getImageId());
            diskImageDynamicDao.remove(disk.getImageId());
            return null;
        });
        return true;
    }
}
