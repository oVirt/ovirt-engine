package org.ovirt.engine.core.dao.network;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkStatistics;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.Dao;

//TODO: Split to 2 interfaces - one for statistics and one for interfaces. Both should extend MassOperation
public interface InterfaceDao extends Dao {
    /**
     * Saves the specified statistics
     *
     * @param stats
     *            the statistics
     */
    void saveStatisticsForVds(VdsNetworkStatistics stats);

    /**
     * Saves the specified VDS interface.
     *
     * @param iface
     *            the interface
     */
    void saveInterfaceForVds(VdsNetworkInterface iface);

    /**
     * Updates the statistics.
     *
     * @param stats
     *            the statistics
     */
    void updateStatisticsForVds(VdsNetworkStatistics stats);

    /**
     * Updates the given collection of vds network statistics using a more efficient method to update all of them at
     * once, rather than each at a time.
     *
     * @param statistics
     *            The collection of statistics to update.
     */
    void massUpdateStatisticsForVds(Collection<VdsNetworkStatistics> statistics);

    /**
     * Updates the specified VDS interface.
     *
     * @param iface
     *            the interface
     */
    void updateInterfaceForVds(VdsNetworkInterface iface);

    /**
     * Updates the given collection of vds network interface using a more efficient method to update all of them at
     * once, rather than each at a time.
     * @param dbIfacesToBatch
     *            The collection of interfaces to update.
     */
    void massUpdateInterfacesForVds(List<VdsNetworkInterface> dbIfacesToBatch);

    /**
     * Clears the networkName from the specified nics.
     */
    void massClearNetworkFromNics(List<Guid> nicIds);

    /**
     * Retrieves all interfaces for the given VDS id.
     *
     * @param id
     *            the VDS id
     * @return the list of interfaces
     */
    List<VdsNetworkInterface> getAllInterfacesForVds(Guid id);

    /**
     * Retrieves all networks names for the given Cluster id,
     * aggregated by its hosts.
     *
     * @param clusterId
     *            the cluster id
     * @return map of host uuid and host's network names.
     */
    Map<Guid, List<String>> getHostNetworksByCluster(Guid clusterId);

    /**
     * Retrieves all interfaces for the given VDS id with optional filtering.
     *
     * @param id
     *            the VDS id
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     * @return the list of interfaces
     */
    List<VdsNetworkInterface> getAllInterfacesForVds(Guid id, Guid userID, boolean isFiltered);

    /**
     * Retrieves the management interface for the given VDS id with optional filtering.
     * @param id
     *            the VDS id
     * @param userID
     *            //TODO: Split to 2 interfaces - one for statistics and one for interfaces. Both should extend MassOp
     *            eration the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     * @return the VDS managed interfaces
     */
    VdsNetworkInterface getManagedInterfaceForVds(Guid id, Guid userID, boolean isFiltered);

    /**
     * Removes the specified statistics.
     *
     * @param id
     *            the statistics
     */
    void removeStatisticsForVds(Guid id);

    /**
     * Removes the VDS interface.
     *
     * @param id
     *            the interface
     */
    void removeInterfaceFromVds(Guid id);

    /**
     * Retrieves the VdsNetworkInterfaces that the given network is attached to.
     *
     * @param networkId
     *            the network
     * @return the list of VdsNetworkInterfaces
     */
    List<VdsNetworkInterface> getVdsInterfacesByNetworkId(Guid networkId);

    /**
     * Returns the VdsNetworkInterface with the specified id.
     *
     * @param id the VdsNetworkInterface Id
     *
     * @return the VdsNetworkInterface having such id.
     */
    VdsNetworkInterface get(Guid id);

    /**
     * Returns the VdsNetworkInterface with the specified name.
     *
     * @param name the VdsNetworkInterface name
     * @param hostId the id of the host
     *
     * @return the VdsNetworkInterface having such id.
     */
    VdsNetworkInterface get(Guid hostId, String name);

    /**
     * Retrieves all interfaces with given IP address from all hosts of the given cluster
     */
    List<VdsNetworkInterface> getAllInterfacesWithIpAddress(Guid clusterId, String ipAddress);

    /**
     * Retrieves all interfaces within a specific cluster
     *
     * @param clusterId
     *            the cluster where the hosts reside in
     */
    List<VdsNetworkInterface> getAllInterfacesByClusterId(Guid clusterId);

    /**
     * Retrieves all interfaces within a specific data center
     *
     * @param dataCenterId
     *            the data center where the hosts reside in
     */
    List<VdsNetworkInterface> getAllInterfacesByDataCenterId(Guid dataCenterId);

    /**
     * @param dataCenterId the date center where the hosts reside in
     * @param label label to check.
     *
     * @return all interfaces within a specific data center
     */
    List<VdsNetworkInterface> getAllInterfacesByLabelForDataCenter(Guid dataCenterId, String label);

    /**
     * Retrieves all interfaces marked with a given label
     *
     * @param clusterId
     *            the cluster where the hosts reside in
     * @param label
     *            the label to search for
     */
    List<VdsNetworkInterface> getAllInterfacesByLabelForCluster(Guid clusterId, String label);

    /**
     * Retrieves all network labels defined on networks in a specific data-center
     *
     * @param id
     *            the data-center id
     * @return all labels defined for the data-center's networks
     */
    Set<String> getAllNetworkLabelsForDataCenter(Guid id);

    /**
     * Retrieve the list of the host endpoints (nics or vlans) that configured as iscsi session
     * initiators to the relevant iscsi target
     *
     * @param hostId
     *             the host id
     * @param storageTargetId
     *             the iscsi target id
     */
    List<VdsNetworkInterface> getIscsiIfacesByHostIdAndStorageTargetId(Guid hostId, String storageTargetId);

    /**
     * @param hostId id of host
     * @return host network interface of the host that is in state "Up" and is attached
     *         to a migration network
     */
    Optional<VdsNetworkInterface> getActiveMigrationNetworkInterfaceForHost(Guid hostId);
}
