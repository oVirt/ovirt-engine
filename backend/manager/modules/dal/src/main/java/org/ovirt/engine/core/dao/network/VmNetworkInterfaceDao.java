package org.ovirt.engine.core.dao.network;

import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ReadDao;

public interface VmNetworkInterfaceDao extends ReadDao<VmNetworkInterface, Guid> {
    /**
     * Retrieves all interfaces for the given VM id.
     *
     * @param id
     *            the Vm id
     * @return the list of interfaces
     */
    List<VmNetworkInterface> getAllForVm(Guid id);

    /**
     * Retrieves all interfaces for the given VM id,
     * with optional filtering
     *
     * @param id
     *            the Vm id
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     * @return the list of interfaces
     */
    List<VmNetworkInterface> getAllForVm(Guid id, Guid userID, boolean isFiltered);

    /**
     * Retrieves all interfaces for the given template id.
     *
     * @param id
     *            the template id
     * @return the list of interfaces
     */
    List<VmNetworkInterface> getAllForTemplate(Guid id);

    /**
     * Retrieves all interfaces for the given template id with optional filtering.
     *
     * @param id
     *           the template id
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     * @return the list of interfaces
     */
    List<VmNetworkInterface> getAllForTemplate(Guid id, Guid userID, boolean isFiltered);

    /**
     * Retrieves the VmNetworkInterfaces that the given network is attached to.
     *
     * @param networkId
     *            the network
     * @return the list of VmNetworkInterfaces
     */
    List<VmNetworkInterface> getAllForNetwork(Guid networkId);

    /**
     * Retrieves the VmTemplate Network Interfaces that the given network is attached to.
     *
     * @param networkId
     *            the network
     * @return the list of VmNetworkInterfaces
     */
    List<VmNetworkInterface> getAllForTemplatesByNetwork(Guid networkId);

    /**
     * Retrieves only the properties of VM Network Interfaces that are relevant for
     * their statistics monitoring.
     *
     * @param vmId
     *             the VM id
     * @return the list of interfaces with partial data for monitoring
     */
    List<VmNetworkInterface> getAllForMonitoredVm(Guid vmId);

    /**
     * Retrieves only the VMs which have one or vNICs out of sync.
     * An out of sync vNIC is a vNIC whose configuration has been updated on engine
     * but the update cannot be applied on the VM until the vNIC is unplugged or the
     * VM is restarted.
     *
     * @return list of VM ids
     */
    List<Guid> getAllWithVnicOutOfSync(Set<Guid> vmIds);

}
