package org.ovirt.engine.core.dao.network;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.GenericDao;
import org.ovirt.engine.core.dao.MassOperationsDao;

public interface VmNicDao extends GenericDao<VmNic, Guid>, MassOperationsDao<VmNic, Guid> {
    /**
     * Retrieves all interfaces for the given VM id.
     *
     * @param id
     *            the Vm id
     * @return the list of interfaces
     */
    List<VmNic> getAllForVm(Guid id);

    /**
     * Retrieves all interfaces for the given template id.
     *
     * @param id
     *            the template id
     * @return the list of interfaces
     */
    List<VmNic> getAllForTemplate(Guid id);

    /**
     * Retrieves the interfaces that the given network is attached to.
     *
     * @param networkId
     *            the network
     * @return the list of VmNetworkInterfaces
     */
    List<VmNic> getAllForNetwork(Guid networkId);

    /**
     * Retrieves 'active' interfaces that the given network is attached to.
     * An active interface is plugged and its VM is not down.
     *
     * @param networkId
     *            the network
     * @return the list of VmNetworkInterfaces
     */
    List<VmNic> getActiveForNetwork(Guid networkId);

    /**
     * Retrieves all 'active' interfaces for the specified profile.
     * An active interface is plugged and its VM is not down.
     *
     * @param vnicProfileId
     *              vnic profile id
     * @return the list of VmNetworkInterfaces
     */
    List<VmNic> getActiveForVnicProfile(Guid vnicProfileId);

    /**
     * Sets all vm interfaces of specified vm to synced
     *
     * @param vmId vm id
     */
    void setVmInterfacesSyncedForVm(Guid vmId);

    /**
     * Retrieves the VmTemplate Network Interfaces that the given network is attached to.
     *
     * @param networkId
     *            the network
     * @return the list of VmNetworkInterfaces
     */
    List<VmNic> getAllForTemplatesByNetwork(Guid networkId);

    /**
     * Retrieves the MAC addresses of the Vms in the given Data Center.
     *
     * @param dataCenterId
     *            the Data Center
     * @return the list of MAC addresses
     */
    List<String> getAllMacsByDataCenter(Guid dataCenterId);

    /**
     * Retrieves the MAC addresses of the Vms in the given Cluster.
     * @param clusterId ID of cluster
     * @return the list of MAC addresses belonging to given Cluster.
     */
    List<String> getAllMacsByClusterId(Guid clusterId);

    /**
     * Retrieves the plugged VmNetworkInterfaces that have the given MAC address
     *
     * @param macAddress
     *            the MAC address
     * @return the list of plugged VmNetworkInterfaces
     */
    List<VmNic> getPluggedForMac(String macAddress);
}
