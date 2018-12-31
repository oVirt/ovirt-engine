package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmBackup;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

/**
 * {@code VmBackupDao} defines a type which performs CRUD operations on instances of {@link VmBackup}.
 */
public interface VmBackupDao extends GenericDao<VmBackup, Guid> {
    /**
     * Retrieves the list of backups for the given VM id.
     *
     * @param id the VM id
     * @return the list of VM backups
     */
    List<VmBackup> getAllForVm(Guid id);

    /**
     * Adds the specified disk to VM backup.
     *
     * @param backupId the VM backup id
     * @param diskId the disk id to add
     */
    void addDiskToVmBackup(Guid backupId, Guid diskId);

    /**
     * Adds the specified backup url to the VM backup.
     *
     * @param backupId the VM backup id
     * @param diskId the relevant disk id
     * @param backupUrl the backup URL
     */
    void addBackupUrlToVmBackup(Guid backupId, Guid diskId, String backupUrl);

    /**
     * Get disks associated with the VM backup.
     *
     * @param backupId the VM backup id
     * @return the list of associated disks.
     */
    List<DiskImage> getDisksByBackupId(Guid backupId);

    /**
     * Remove all disks from VM backup
     *
     * @param backupId the VM backup id
     */
    void removeAllDisksFromBackup(Guid backupId);

}
