package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;

public interface VmDeviceDao extends GenericDao<VmDevice, VmDeviceId>, MassOperationsDao<VmDevice, VmDeviceId> {

    /**
     * Check if the {@link VmDevice} with the given id exists or not.
     * @param id
     *            The device id.
     * @return Does the device exist or not.
     */
    boolean exists(VmDeviceId id);

    List<VmDevice> getVmDeviceByVmId(Guid vmId);

    List<VmDevice> getVmDeviceByVmId(Guid vmId, Guid userID, boolean isFiltered);

    List<VmDevice> getVmDevicesByDeviceId(Guid deviceId, Guid vmId);

    List<VmDevice> getVmDeviceByVmIdAndType(Guid vmId, VmDeviceGeneralType type);

    /**
     * @param vmBaseId VM or template id
     */
    List<VmDevice> getVmDeviceByVmIdTypeAndDevice(Guid vmBaseId, VmDeviceGeneralType type, String device);

    List<VmDevice> getVmDeviceByVmIdTypeAndDevice(Guid vmBaseId, VmDeviceGeneralType type, VmDeviceType device);

    List<VmDevice> getVmDeviceByVmIdTypeAndDevice(Guid vmId,
            VmDeviceGeneralType type,
            String device,
            Guid userID,
            boolean isFiltered);

    List<VmDevice> getVmDeviceByTypeAndDevice(List<Guid> vmsIds,
            VmDeviceGeneralType type,
            String device,
            Guid userID,
            boolean isFiltered);

    List<VmDevice> getVmDeviceByType(VmDeviceGeneralType type);

    List<VmDevice> getUnmanagedDevicesByVmId(Guid vmId);

    boolean existsVmDeviceByVmIdAndType(Guid vmId, VmDeviceGeneralType type);

    boolean isMemBalloonEnabled(Guid vmId);

    void removeAll(List<VmDeviceId> removedDeviceIds);

    void removeVmDevicesByVmIdAndType(Guid vmId, VmDeviceGeneralType type);

    void saveAll(List<VmDevice> newVmDevices);

    /**
     * Clear the device address kept for this device. When A VM is started, devices with empty addresses are allocated
     * with new one. Device addresses are fetched by the engine from each host per each VM periodically so once a VM is
     * up, its devices map is fetched and saved to DB.
     * Use this method when the address is not used anymore or is not valid e.g when changing a disk interface type from
     * IDE to VirtIO.
     */
    void clearDeviceAddress(Guid deviceId);

    /**
     * Clear addresses of all devices associated with the given VM.
     *
     * @see #clearDeviceAddress
     */
    void clearAllDeviceAddressesByVmId(Guid vmId);

    /**
     * Remove all unmanaged devices associated with the given VM.
     */
    void removeAllUnmanagedDevicesByVmId(Guid vmId);

}
