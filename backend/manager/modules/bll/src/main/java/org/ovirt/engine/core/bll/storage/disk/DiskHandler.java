package org.ovirt.engine.core.bll.storage.disk;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;

@Singleton
public class DiskHandler {

    @Inject
    private DiskDao diskDao;

    @Inject
    private DiskImageDao diskImageDao;

    /**
     * loads the disk info for the active snapshot, for luns the lun disk will be returned.
     */
    public Disk loadActiveDisk(Guid diskId) {
        return diskDao.get(diskId);
    }

    /**
     * Loads a disk info for selected snapshot, if no snapshot ID was specified it will load the active snapshot
     */
    public Disk loadDiskFromSnapshot(Guid diskId, Guid snapshotId) {
        return snapshotId == null ?  loadActiveDisk(diskId) :
                diskImageDao.getDiskSnapshotForVmSnapshot(diskId, snapshotId);
    }
}
