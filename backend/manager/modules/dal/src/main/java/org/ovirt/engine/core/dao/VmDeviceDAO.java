package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.compat.Guid;

public interface VmDeviceDAO extends GenericDao<VmDevice, VmDeviceId> {

    /**
     * Check if the {@link VmDevice} with the given id exists or not.
     * @param id
     *            The device id.
     * @return Does the device exist or not.
     */
    boolean exists(VmDeviceId id);

    List<VmDevice> getVmDeviceByVmId(Guid vmId);

    List<VmDevice> getVmDeviceByVmIdAndType(Guid vmId, String type);

    List<VmDevice> getVmDeviceByVmIdTypeAndDevice(Guid vmId, String type, String device);
    List<VmDevice> getVmDeviceByVmIdTypeAndDevice(Guid vmId, String type, String device, Guid userID, boolean isFiltered);
}
