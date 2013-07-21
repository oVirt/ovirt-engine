package org.ovirt.engine.core.dao.network;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.GenericDao;

public interface VmNicDao extends GenericDao<VmNic, Guid> {
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

    /***
     * Retrieves the plugged VmNetworkInterfaces that have the given MAC address
     *
     * @param macAddress
     *            the MAC address
     * @return the list of plugged VmNetworkInterfaces
     */
    List<VmNic> getPluggedForMac(String macAddress);
}
