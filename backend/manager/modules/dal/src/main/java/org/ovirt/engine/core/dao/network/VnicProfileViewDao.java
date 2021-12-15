package org.ovirt.engine.core.dao.network;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ReadDao;
import org.ovirt.engine.core.dao.SearchDao;

public interface VnicProfileViewDao extends ReadDao<VnicProfileView, Guid>, SearchDao<VnicProfileView> {

    /**
     * Retrieves a vnic profile.
     *
     * @param id
     *            the vnic profile's ID
     * @param userId
     *            the id of the user performing the query
     * @param filtered
     *            does the query should be filtered by the user
     *
     * @return the list of vnic profiles
     */
    VnicProfileView get(Guid id, Guid userId, boolean filtered);

    /**
     * Retrieves all vnic profiles.
     *
     * @param userId
     *            the id of the user performing the query
     * @param filtered
     *            does the query should be filtered by the user
     *
     * @return the list of vnic profiles
     */
    List<VnicProfileView> getAll(Guid userId, boolean filtered);

    /**
     * Retrieves all vnic profiles for the given data center.
     *
     * @param id
     *            the data center's ID
     * @return the list of vnic profiles
     */
    List<VnicProfileView> getAllForDataCenter(Guid id);

    /**
     * Retrieves all vnic profiles for the given data center.
     *
     * @param id
     *            the data center's ID
     * @param userId
     *            the id of the user performing the query
     * @param filtered
     *            does the query should be filtered by the user
     *
     * @return the list of vnic profiles
     */
    List<VnicProfileView> getAllForDataCenter(Guid id, Guid userId, boolean filter);

    /**
     * Retrieves all vnic profiles for the given cluster.
     *
     * @param id
     *            the cluster ID
     * @return the list of vnic profiles
     */
    List<VnicProfileView> getAllForCluster(Guid id);

    /**
     * Retrieves all vnic profiles for the given cluster.
     *
     * @param id
     *            the cluster ID
     * @param userId
     *            the id of the user performing the query
     * @param filter
     *            does the query should be filtered by the user
     *
     * @return the list of vnic profiles
     */
    List<VnicProfileView> getAllForCluster(Guid id, Guid userId, boolean filter);

    /**
     * Retrieves all vnic profiles associated to the given network.
     *
     * @param networkId
     *            the network's ID
     * @return the list of vnic profiles
     */
    List<VnicProfileView> getAllForNetwork(Guid networkId);

    /**
     * Retrieves all vnic profiles associated to the given network.
     *
     * @param networkId
     *            the network's ID
     * @param userId
     *            the id of the user performing the query
     * @param filtered
     *            does the query should be filtered by the user
     * @return the list of vnic profiles
     */
    List<VnicProfileView> getAllForNetwork(Guid networkId, Guid userId, boolean filtered);


    /**
     * Retrieves all vnic profiles associated with the given network QoS.
     * @param qosId
     *          the network QoS ID
     * @return the list of vnic profiles
     */
    List<VnicProfileView> getAllForNetworkQos(Guid qosId);
}
