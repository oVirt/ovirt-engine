package org.ovirt.engine.core.dao;

import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.compat.Guid;

public interface DiskVmElementDao extends GenericDao<DiskVmElement, VmDeviceId> {

    /**
     * Retrieves a disk VM elements by its id.
     *
     * @param id
     *            the ID of the disk VM element
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     *
     * @return the disk VM element with the given id or null if none found
     */
    DiskVmElement get(VmDeviceId id, Guid userID, boolean isFiltered);

    /**
     * Retrieves all disk VM elements for the specified disk id.
     * @param diskId
     *          the disk id.
     * @return the list of disk VM elements.
     */
    List<DiskVmElement> getAllDiskVmElementsByDiskId(Guid diskId);

    /**
     * Retrieves all disk VM elements for the specified disks ids.
     * @param disksIds
     *          the disks ids.
     * @return the list of disk VM elements.
     */
    List<DiskVmElement> getAllDiskVmElementsByDisksIds(Collection<Guid> disksIds);

    /**
     * Retrieves all disk VM elements for the specified virtual machine id.
     *
     * @param vmId
     *            the VM id
     * @return the list of disk VM elements
     */
    List<DiskVmElement> getAllForVm(Guid vmId);

    /**
     * Retrieves all disk VM elements for the specified virtual machine id.
     *
     * @param vmId
     *            the VM id
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     *
     * @return the list of disk VM elements
     */
    List<DiskVmElement> getAllForVm(Guid vmId, Guid userID, boolean isFiltered);

    /**
     * Retrieves all disk VM elements for plugged disk attached to the specified virtual machine id.
     *
     * @param vmId
     *            the VM id
     * @return the list of disk VM elements
     */
    List<DiskVmElement> getAllPluggedToVm(Guid vmId);

}
