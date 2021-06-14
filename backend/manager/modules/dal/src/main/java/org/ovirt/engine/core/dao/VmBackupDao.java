package org.ovirt.engine.core.dao;

import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmBackup;
import org.ovirt.engine.core.common.businessentities.VmBackupPhase;
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
     * Returns the backup_url for the specified disk.
     *
     * @param backupId the VM backup id
     * @param diskId the relevant disk id
     */
    String getBackupUrlForDisk(Guid backupId, Guid diskId);

    /**
     * Get disks associated with the VM backup.
     *
     * @param backupId the VM backup id
     * @return the list of associated disks.
     */
    List<DiskImage> getDisksByBackupId(Guid backupId);

    /**
     * Deletes completed backups.
     * Successful backups have {@link VmBackupPhase#SUCCEEDED} status.
     * Failed backups have {@link VmBackupPhase#FAILED} status.
     *
     * @param succeededBackups all successful backups having older end time than this date will be deleted.
     * @param failedBackups all failed backups having older end time than this date will be deleted.
     */
    void deleteCompletedBackups(Date succeededBackups, Date failedBackups);
}
