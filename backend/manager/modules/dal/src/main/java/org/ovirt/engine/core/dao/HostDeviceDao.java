package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.HostDeviceId;
import org.ovirt.engine.core.common.businessentities.HostDeviceView;
import org.ovirt.engine.core.compat.Guid;

import java.util.List;

public interface HostDeviceDao extends GenericDao<HostDevice, HostDeviceId>, MassOperationsDao<HostDevice, HostDeviceId> {

    List<HostDevice> getHostDevicesByHostId(Guid hostId);

    List<HostDevice> getHostDevicesByHostIdAndIommuGroup(Guid hostId, int iommuGroup);

    List<HostDeviceView> getVmExtendedHostDevicesByVmId(Guid vmId);

    List<HostDeviceView> getExtendedHostDevicesByHostId(Guid hostId);

    HostDevice getHostDeviceByHostIdAndDeviceName(Guid hostId, String deviceName);

    boolean checkVmHostDeviceAvailability(Guid vmId, Guid hostId);

    void markHostDevicesUsedByVmId(Guid vmId);

    void freeHostDevicesUsedByVmId(Guid vmId);
}
