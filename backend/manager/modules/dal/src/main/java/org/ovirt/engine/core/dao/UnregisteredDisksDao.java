package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.UnregisteredDisk;
import org.ovirt.engine.core.common.businessentities.storage.UnregisteredDiskId;
import org.ovirt.engine.core.compat.Guid;

public interface UnregisteredDisksDao extends Dao, MassOperationsDao<UnregisteredDisk, UnregisteredDiskId> {
    /**
     * Retrieves the disk with the given entityId and storage domain id.<BR/>
     * If the Storage Domain id is null, then return all the unregistered disks with the diskId.<BR/>
     * If the disk id is null, then return all the unregistered disks of the Storage Domain id.<BR/>
     * If the disk id and Storage Domain id is null, then return all the unregistered disks in unregistered disks.
     *
     * @param diskId
     *            The Disk Id.
     * @param storageDomainId
     *            The Storage Domain Id.
     * @return The entity instance, or {@code null} if not found.
     */
    public List<UnregisteredDisk> getByDiskIdAndStorageDomainId(Guid diskId, Guid storageDomainId);

    /**
     * Insert a new disk entity to the unregistered table.
     */
    public void saveUnregisteredDisk(UnregisteredDisk disk);

    /**
     * Remove a disk from the unregistered table. If the Storage Domain id is null, then remove the unregistered disk
     * with the diskId.<BR/>
     * If the disk id is null, then remove all the unregistered disks of this Storage Domain id.<BR/>
     * If the disk id and Storage Domain id is null, then remove all the unregistered disks in unregistered disks.
     *
     * @param diskId
     *            The Disk Id.
     * @param storageDomainId
     *            The Storage Domain Id.
     */
    public void removeUnregisteredDisk(Guid diskId, Guid storageDomainId);

    /**
     * Remove a disk from the unregistered table related to VM.
     *
     * @param vmId
     *            The VM Id.
     * @param storageDomainId
     *            The Storage Domain Id.
     */
    public void removeUnregisteredDiskRelatedToVM(Guid vmId, Guid storageDomainId);
}
