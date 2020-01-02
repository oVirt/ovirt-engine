package org.ovirt.engine.core.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.compat.Guid;

public interface DiskDao extends ReadDao<Disk, Guid>, SearchDao<Disk> {

    /**
     * Retrieves all disks for the specified virtual machine id.
     *
     * @param vmId
     *            the VM id
     * @return the list of disks
     */
    List<Disk> getAllForVm(Guid vmId);

    /**
     * Retrieves all disks for each vm passed
     *
     * @param vmIds
     *              list of VM ids
     * @return map storing disks for each VM
     */
    Map<Guid, List<Disk>> getAllForVms(Collection<Guid> vmIds);

    /**
     * Retrieves all disks for the specified virtual machine id.
     *
     * @param vmId
     *            the VM id
     * @param onlyPluggedDisks
     *            whether to returned only the disks plugged to the VM or not
     * @return the list of disks
     */
    List<Disk> getAllForVm(Guid vmId, boolean onlyPluggedDisks);

    /**
     * Retrieves all disks for the specified virtual machine id,
     * with optional filtering
     *
     * @param vmId
     *            the VM id
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions

     * @return the list of disks
     */
    List<Disk> getAllForVm(Guid vmId, Guid userID, boolean isFiltered);

    /**
     * Retrieves all disks for the specified virtual machine id, with optional filtering
     *
     * @param vmId
     *            the VM id
     * @param onlyPluggedDisks
     *            whether to returned only the disks plugged to the VM or not
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     *
     * @return the list of disks
     */
    List<Disk> getAllForVm(Guid vmId, boolean onlyPluggedDisks, Guid userID, boolean isFiltered);

    /**
     * Retrieves a list of disk IDs with missing/damaged snapshot for a specified virtual machine id.
     *
     * @param vmId
     *            the VM id
     *
     * @return the list of disk ids
     */
    List<Guid> getImagesWithDamagedSnapshotForVm(Guid vmId);

    /**
     * Retrieves a list of disk IDs with missing/damaged snapshot for a specified virtual machine id,
     * with optional filtering
     *
     * @param vmId
     *            the VM id
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     *
     * @return the list of disk ids
     */
    List<Guid> getImagesWithDamagedSnapshotForVm(Guid vmId, Guid userID, boolean isFiltered);

    /**
     * Retrieves all disks for the specified user
     * with optional filtering
     *
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions

     * @return the list of disks
     */
    public List<Disk> getAll(Guid userID, boolean isFiltered);

    /**
     * Retrieves all shared disks for the specified storage pool/virtual machine id,
     * @param vmId if vmId=null retrieve all shared disks in SP
     */
    List<Disk> getAllAttachableDisksByPoolId(Guid poolId, Guid vmId , Guid userId, boolean isFiltered);

    /**
     * Retrieves the boot active disk of the VM with the specified id, null if no boot active disk is attached to this VM.
     *
     * @param vmId
     *            the VM id
     * @return The active boot disk that is attached to the specified VM, null if no attached active disk is defined as boot.
     */
    Disk getVmBootActiveDisk(Guid vmId);

    /**
     * Returns the Disk with the specified id, with optional filtering.
     *
     * @param diskId
     *            the Disk id
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     * @return the Disk
     */
    Disk get(Guid diskId, Guid userID, boolean isFiltered);

    /**
     * Retrieves all disks for the specified virtual machine id, with optional filtering. Only data for the UI basic
     * screen is returned
     * @param vmId
     *            the VM id
     * @param onlyPluggedDisks
     *            whether to returned only the disks plugged to the VM or not
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     * @return the list of disks
     */
    List<Disk> getAllForVmPartialData(Guid vmId, boolean onlyPluggedDisks, Guid userID, boolean isFiltered);

    /**
     * Retrieves all disks filtered by a specified disk storage type.
     *
     * @param diskStorageType
     *            the disk storage type
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions

     * @return the list of disks
     */
    public List<Disk> getAllFromDisksByDiskStorageType(DiskStorageType diskStorageType, Guid userID, boolean isFiltered);

    /**
     * Retrieves all disks including all the snapshots.
     *
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions

     * @return the list of disks
     */
    public List<Disk> getAllFromDisksIncludingSnapshots(Guid userID, boolean isFiltered);

    /**
     * Retrieves all disks for the specified disk id, with optional filtering.
     *
     * @param diskId
     *            the disk id
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions

     * @return the list of disks
     */
    public List<Disk> getAllFromDisksIncludingSnapshotsByDiskId(Guid diskId, Guid userID, boolean isFiltered);
}
