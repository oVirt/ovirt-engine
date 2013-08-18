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
     * Retrieves all disks for the specified virtual machine id.
     *
     * @param id
     *            the VM id
     * @param onlyPluggedDisks
     *            whether to returned only the disks plugged to the VM or not
     * @return the list of disks
     */
    List<Disk> getAllForVm(Guid id, boolean onlyPluggedDisks);

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

    /**
     * Retrieves all disks for the specified virtual machine id, with optional filtering
     *
     * @param id
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
    List<Disk> getAllForVm(Guid id, boolean onlyPluggedDisks, Guid userID, boolean isFiltered);

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
     * @param poolId
     * @param vmId, if vmId=null retrieve all shared disks in SP
     * @param userId
     * @param isFiltered
     * @return
     */
    List<Disk> getAllAttachableDisksByPoolId(Guid poolId, Guid vmId ,Guid userId, boolean isFiltered);

    /**
     * Retrieves the boot disk of the VM with the specified id, null if no boot disk is attached to this VM.
     *
     * @param vmId
     *            the VM id
     * @return The boot disk that is attached to the specified VM, null if no attached disk is defined as boot.
     */
    Disk getVmBootDisk(Guid vmId);

    /**
     * Returns the Disk with the specified id, with optional filtering.
     *
     * @param id
     *            the Disk id
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     * @return the Disk
     */
    Disk get(Guid id, Guid userID, boolean isFiltered);

    /**
     * Retrieves all disks for the specified virtual machine id, with optional filtering. Only data for the UI basic
     * screen is returned
     * @param id
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
}
