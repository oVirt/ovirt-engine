package org.ovirt.engine.core.bll.network.host;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

public interface NetworkDeviceHelper {

    /**
     * Retrieves the <code>VdsNetworkInterface</code> that the specified <code>pciDevice</code> represents.
     *
     * @return the <code>VdsNetworkInterface</code> that the specified <code>pciDevice</code> represents. If the device
     *         is not parent of network interface device or doesn't exist in the VdsInterface table a <code>null</code>
     *         is returned.
     */
    VdsNetworkInterface getNicByPciDevice(final HostDevice pciDevice);

    /**
     * Retrieves the <code>VdsNetworkInterface</code> that the specified <code>pciDevice</code> represents.
     * This method uses the specified <code>devices</code> and doesn't fetch data from the DB.
     *
     * @param devices collection of all the devices.
     * @return the <code>VdsNetworkInterface</code> that the specified <code>pciDevice</code> represents. If the device
     *         is not parent of network interface device or doesn't exist in the VdsInterface table a <code>null</code>
     *         is returned.
     */
    VdsNetworkInterface getNicByPciDevice(final HostDevice pciDevice, Collection<HostDevice> devices);

    /**
     * Retrieves whether the specified <code>device</code> is SR-IOV enabled.
     *
     * @return whether the specified <code>device</code> is SR-IOV enabled
     */
    boolean isSriovDevice(HostDevice device);

    /**
     * Retrieves whether the specified <code>device</code> represents a physical nic.
     *
     * @return whether the specified <code>device</code> represents a physical nic
     */
    boolean isNetworkDevice(HostDevice device);

    /**
     * Adds <code>maxNumOfVfs</code> and <code>numOfVfs</code> info to the <code>hostNicVfsConfig</code>
     */
    void updateHostNicVfsConfigWithNumVfsData(HostNicVfsConfig hostNicVfsConfig);

    /**
     * Retrieves all the HostDevices of the specified host, adds <code>maxNumOfVfs</code> and <code>numOfVfs</code> info
     * to each <code>HostDevice</code>
     *
     * @return updated HostDevices of the specified host.
     */
    List<HostNicVfsConfig> getHostNicVfsConfigsWithNumVfsDataByHostId(Guid hostId);

    /**
     * Retrieves whether all the VFs on the nic are free to use by a VM
     *
     * @param nic
     *            physical SR-IOV enabled nic
     * @return whether all the VFs on the nic are free to use by a VM.
     * @throws UnsupportedOperationException in case the nic is not SR-IOV enabled
     */
    boolean areAllVfsFree(VdsNetworkInterface nic);

    /**
     * Retrieves whether the device is occupied by virtual network or VLAN
     *
     * @param hostDevice arbitrary physical host device (not only network)
     * @return whether this device is not occupied for networking purposes
     */
    boolean isDeviceNetworkFree(HostDevice hostDevice);

    /**
     * Retrieves the first free VF on the nic
     *
     * @param nic
     *            physical SR-IOV enabled nic
     * @param excludeVfs
     *            vfs that should be considered as non-free
     * @return the first free VF on the nic
     * @throws UnsupportedOperationException in case the nic is not SR-IOV enabled
     */
    HostDevice getFreeVf(VdsNetworkInterface nic, List<String> excludeVfs);

    /**
     * Retrieves the pciDevice name of the specified <code>nic</code>
     *
     * @return the pciDevice name of the specified <code>nic</code>
     */
    String getPciDeviceNameByNic(VdsNetworkInterface nic);

    /**
     * This method updates the DB to reflect that the specified VFs are attached to the specified VM. Such VFs will be
     * considered as used. Passing <code>null</code> as <code>vmId</code> means the VF should not be attached to any VM,
     * and will be considered by system as free to use.
     */
    void setVmIdOnVfs(Guid hostId, Guid vmId, final Set<String> vfsNames);

    /**
     * Removes the <code>vmId</code> from all the VFs that were attached to the VM
     *
     * @return the id of the affected Host or null if there were no VFs attached to the VM
     */
    Guid removeVmIdFromVfs(final Guid vmId);

    /**
     * Retrieves the relation between VFs and the PFs they rely on for SR-IOV enabled NICs on the given host.
     *
     * @param hostId the host Id
     * @return the relation between the host VF NICs and PF NICs they rely on.
     * The map key is VF NIC id and the map value is PF NIC id.
     */
    Map<Guid, Guid> getVfMap(Guid hostId);
}
