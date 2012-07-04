package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.NetworkClusterId;
import org.ovirt.engine.core.common.businessentities.network_cluster;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>NetworkClusterDAO</code> defines a type for performing CRUD operations on instances of {@link network_cluster}.
 *
 *
 */
public interface NetworkClusterDAO extends DAO {

    /**
     * Retrieves the entity with the given id.
     *
     * @param id
     *            The id to look by (can't be <code>null</code>).
     * @return The entity instance, or <code>null</code> if not found.
     */
    public network_cluster get(NetworkClusterId id);

    /**
     * Retrieves all network clusters.
     *
     * @return the list of network clusters
     */
    List<network_cluster> getAll();

    /**
     * Retrieves all network clusters for the specified cluster.
     *
     * @param the
     *            network cluster
     * @return the list of clusters
     */
    List<network_cluster> getAllForCluster(Guid cluster);

    /**
     * Retrieves all network clusters for the specified network.
     *
     * @param network
     *            the network
     * @return
     */
    List<network_cluster> getAllForNetwork(Guid network);

    /**
     * Saves the new network cluster.
     *
     * @param cluster
     *            the network cluster
     */
    void save(network_cluster cluster);

    /**
     * Updates the network cluster.
     *
     * @param cluster
     *            the network cluster
     */
    void update(network_cluster cluster);

    /**
     * Updates the network cluster status.
     *
     * @param cluster
     *            the network cluster
     */
    void updateStatus(network_cluster cluster);
    /**
     * Removes the specified network from the specified cluster.
     *
     * @param clusterid
     *            the cluster
     * @param networkid
     *            the network
     */
    void remove(Guid clusterid, Guid networkid);

    /**
     * Sets this cluster network as the only display network on the cluster.
     *
     * @param clusterId
     * @param networkId
     */
    void setNetworkExclusivelyAsDisplay(Guid clusterId, Guid networkId);
}
