package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.compat.Guid;

public interface DiskDao extends ReadDao<Disk, Guid>, SearchDAO<Disk> {

    /**
     * Retrieves all disks for the specified virtual machine id.
     *
     * @param id
     *            the VM id
     * @return the list of disks
     */
    List<Disk> getAllForVm(Guid id);

    /**
     * Retrieves all disks for the specified virtual machine id,
     * with optional filtering
     *
     * @param id
     *            the VM id
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions

     * @return the list of disks
     */
    List<Disk> getAllForVm(Guid id, Guid userID, boolean isFiltered);

    List<Disk> getAllAttachableDisksByPoolId(Guid poolId, Guid userId, boolean isFiltered);
}
