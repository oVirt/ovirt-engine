package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmCheckpoint;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

/**
 * {@code VmCheckpointDao} defines a type which performs CRUD operations on instances of {@link VmCheckpoint}.
 */
public interface VmCheckpointDao extends GenericDao<VmCheckpoint, Guid> {
    /**
     * Retrieves the list of checkpoints for the given VM id.
     *
     * @param id
     *            the VM id
     * @return the list of checkpoints
     */
    List<VmCheckpoint> getAllForVm(Guid id);

    /**
     * Adds the specified disk to checkpoint.
     *
     * @param checkpointId the checkpoint id
     * @param diskId the disk id to add
     */
    void addDiskToCheckpoint(Guid checkpointId, Guid diskId);

    /**
     * Get disks associated with the VM checkpoint.
     * @param checkpointId the VM checkpoint id.
     * @return the list of associated disks.
     */
    List<DiskImage> getDisksByCheckpointId(Guid checkpointId);

    /**
     * Remove all disks from checkpoint
     *
     * @param checkpointId the checkpoint id
     */
    void removeAllDisksFromCheckpoint(Guid checkpointId);
}
