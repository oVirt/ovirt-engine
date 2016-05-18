package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.compat.Guid;

public interface DiskVmElementDao extends GenericDao<DiskVmElement, VmDeviceId> {
    /**
     * Retrieves all disk VM elements for the specified virtual machine id.
     *
     * @param vmId
     *            the VM id
     * @return the list of disk VM elements
     */
    List<DiskVmElement> getAllForVm(Guid vmId);

    /**
     * Retrieves all disk VM elements for plugged disk attached to the specified virtual machine id.
     *
     * @param vmId
     *            the VM id
     * @return the list of disk VM elements
     */
    List<DiskVmElement> getAllPluggedToVm(Guid vmId);

}
