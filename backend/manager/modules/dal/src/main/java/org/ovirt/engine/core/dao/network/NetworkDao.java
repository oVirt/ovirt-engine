package org.ovirt.engine.core.dao.network;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.GenericDao;

public interface NetworkDao extends GenericDao<Network, Guid> {

    /**
     * Retrieves all networks.
     *
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     * @return the list of networks
     */
    List<Network> getAll(Guid userID, boolean isFiltered);

    /**
     * Retrieves the network with the specified name and storage pool id.
     *
     * @param name
     *            the network name
     * @param storagePoolId
     *            the network's storage pool id
     * @return the network
     */
    Network getByNameAndDataCenter(String name, Guid storagePoolId);

    /**
     * Retrieves the network with the specified name which is attached to a specific Cluster.
     *
     * @param name
     *            the network name
     * @param clusterId
     *            the cluster the network is attached to
     * @return the network
     */
    Network getByNameAndCluster(String name, Guid clusterId);

    /**
     * Retrieves all networks for the given data center.
     *
     * @param id
     *            the data center
     * @return the list of networks
     */
    List<Network> getAllForDataCenter(Guid id);

    /**
     * Retrieves all networks for the given data center.
     *
     * @param id
     *            the data center
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     * @return the list of networks
     */
    List<Network> getAllForDataCenter(Guid id, Guid userID, boolean isFiltered);

    /**
     * Retrieves all networks for the given cluster.
     *
     * @param id
     *            the cluster
     * @return the list of networks
     */
    List<Network> getAllForCluster(Guid id);

    /**
     * Retrieves all networks for the given cluster with optional permission filtering.
     *
     * @param id
     *            the cluster
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     * @return the list of networks
     */
    List<Network> getAllForCluster(Guid id, Guid userID, boolean isFiltered);

    /**
     * Retrieves all networks using a given QoS entity.
     *
     * @param qosId
     *            the ID of the QoS entity
     * @return the list of networks
     */
    List<Network> getAllForQos(Guid qosId);

    /**
     * Retrieves all networks for the given provider.
     *
     * @param id
     *            the provider's ID
     * @return the list of networks
     */
    List<Network> getAllForProvider(Guid id);

    /**
     * Retrieves all network labels defined on networks in a specific data-center
     *
     * @param id
     *            the data-center id
     * @return all labels defined for the data-center's networks
     */
    Set<String> getAllNetworkLabelsForDataCenter(Guid id);

    /**
     * Retrieve a specific network.
     *
     * @param id
     *            the network id
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     * @return the network
     */
    Network get(Guid networkId, Guid userID, boolean isFiltered);

    /**
     * Retrieves the networks with the specified label which are attached to a specific Cluster.
     *
     * @param label
     *            the network label
     * @param clusterId
     *            the cluster the networks are attached to
     * @return the networks
     */
    List<Network> getAllByLabelForCluster(String label, Guid clusterId);

    /**
     * Retrieves the management network for the given cluster.
     *
     * @param clusterId
     *            the cluster the network is attached to
     *
     * @return the management {@link Network}
     */
    Network getManagementNetwork(Guid clusterId);

    /**
     * Retrieves the management networks for the given DC.
     *
     * @param dataCenterId
     *            the data center the network is belongs to
     *
     * @return the management {@link Network}s
     */
    List<Network> getManagementNetworks(Guid dataCenterId);

    /**
     * Retrieves the list of external network linked to given physical network
     *
     * @param physicalNetworkId the physical network id
     * @return the external {@link Network}s
     */
    List<Network> getAllExternalNetworksLinkedToPhysicalNetwork(Guid physicalNetworkId);

    /**
     * Retrieves a list of required networks for the given DC.
     *
     * @param dataCenterId
     *            the data center the networks belong to
     *
     * @return the required networks {@link Network}s
     */
    List<Network> getRequiredNetworksByDataCenterId(Guid dataCenterId);

    /**
     * Retrieves a network with given vdsm name and data center id
     *
     * @param vdsmName     Vdsm name of the network
     * @param dataCenterId Id of the data center
     * @return the {@link Network}
     */
    Network getNetworkByVdsmNameAndDataCenterId(String vdsmName, Guid dataCenterId);

    Map<String, Network> getNetworksForCluster(Guid clusterId);
}
