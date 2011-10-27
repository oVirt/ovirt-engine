package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>NetworkDAO</code> defines a type for performing CRUD operations on instances of {@link network}.
 *
 *
 */
public interface NetworkDAO extends GenericDao<network, Guid> {
    /**
     * Retrieves the network with the specified name.
     *
     * @param name
     *            the network name
     * @return the network
     */
    network getByName(String name);

    /**
     * Retrieves all networks for the given data center.
     *
     * @param id
     *            the data center
     * @return the list of networks
     */
    List<network> getAllForDataCenter(Guid id);

    /**
     * Retrieves all networks for the given cluster.
     *
     * @param id
     *            the cluster
     * @return the list of networks
     */
    List<network> getAllForCluster(Guid id);
}
