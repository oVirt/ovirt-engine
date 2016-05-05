package org.ovirt.engine.core.bll.storage.disk;

import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;

@Singleton
public class DiskHandler {
    /**
     * loads the disk info for the active snapshot, for luns the lun disk will be returned.
     */
    public Disk loadActiveDisk(Guid diskId) {
        return getDiskDao().get(diskId);
    }

    /**
     * Loads a disk info for selected snapshot, if no snapshot ID was specified it will load the active snapshot
     */
    public Disk loadDiskFromSnapshot(Guid diskId, Guid snapshotId) {
        return snapshotId == null ?  loadActiveDisk(diskId) :
                getDiskImageDao().getDiskSnapshotForVmSnapshot(diskId, snapshotId);
    }

    private static DiskDao getDiskDao() {
        return DbFacade.getInstance().getDiskDao();
    }

    private static DiskImageDao getDiskImageDao() {
        return DbFacade.getInstance().getDiskImageDao();
    }
}
