package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.HostDeviceId;
import org.ovirt.engine.core.compat.Guid;

import java.util.List;

public interface HostDeviceDao extends GenericDao<HostDevice, HostDeviceId>, MassOperationsDao<HostDevice, HostDeviceId> {

    List<HostDevice> getHostDevicesByHostId(Guid hostId);

    HostDevice getHostDeviceByHostIdAndDeviceName(Guid hostId, String deviceName);
}
