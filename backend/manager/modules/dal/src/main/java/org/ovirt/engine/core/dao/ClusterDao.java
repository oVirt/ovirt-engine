package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.compat.Guid;

/**
 * {@code ClusterDao} defines a type that performs CRUD operations on instances of {@link Cluster}.
 */
public interface ClusterDao extends GenericDao<Cluster, Guid>, SearchDao<Cluster> {
    /**
     * Gets the group with the specified id.
     *
     * @param id
     *            the group id
     * @return the group
     */
    Cluster get(Guid id);

    /**
     * Gets the group with the specified id, with optional permission filtering.
     *
     * @param id
     *            the group id
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     * @return the group
     */
    Cluster get(Guid id, Guid userID, boolean isFiltered);

    /**
     * Returns the specified VDS group if it has running VMs.
     *
     * @param id
     *            the group id
     * @return the VDS group
     */
    Cluster getWithRunningVms(Guid id);

    /**
     * Returns the specified VDS group if it does not have any VMs or clusters.
     *
     * @param id
     *            the group id
     * @return the VDS group
     */
    Boolean getIsEmpty(Guid id);

    /**
     * Retrieves the group with the specified name.
     *
     * @param name
     *            the group name
     * @return the group
     */
    Cluster getByName(String name);

    /**
     * Retrieves the groups with the specified name.
     *
     * @param name
     *            the group name
     * @param isCaseSensitive
     *            whether to do case-sensitive get or not
     * @return the group
     */
    List<Cluster> getByName(String name, boolean isCaseSensitive);

    /**
     * Retrieves the group with the specified name for the user
     *
     * @param name
     *            the group name
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     * @return the group
     */
    Cluster getByName(String name, Guid userID, boolean isFiltered);
    /**
     * Retrieves the list of groups associated with the given storage pool.
     *
     * @param id
     *            the storage pool
     * @return the list of groups
     */
    List<Cluster> getAllForStoragePool(Guid id);

    /**
     * Retrieves the list of groups associated with the given storage pool, with optional permission filtering.
     *
     * @param id
     *            the storage pool
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     * @return the list of groups
     */
    List<Cluster> getAllForStoragePool(Guid id, Guid userID, boolean isFiltered);

    /**
     * Retrieves all VDS groups.
     *
     * @return the list of groups
     */
    List<Cluster> getAll();

    /**
     * Retrieves all VDS groups.
     *
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     * @return the list of groups
     */
    List<Cluster> getAll(Guid userID, boolean isFiltered);

    /**
     * Saves the supplied group.
     *
     * @param group
     *            the group
     */
    void save(Cluster group);

    /**
     * Updates the group.
     *
     * @param group
     *            the group
     */
    void update(Cluster group);

    /**
     * Removes the VDS group with the given id.
     *
     * @param id
     *            the group id
     */
    void remove(Guid id);

    /**
     * Retries clusters which the given users has permission to perform the given action.
     *
     * @return list of clusters
     */
    List<Cluster> getClustersWithPermittedAction(Guid userId, ActionGroup actionGroup);

    /**
     * Retrieves clusters having valid hosts added to them
     *
     * @return list of clusters
     */
    List<Cluster> getClustersHavingHosts();

    /**
     * Sets the cluster's emulated machine value.
     * @param detectEmulatedMachine - either the cluster emulated machine should be auto detected and set
     */
    void setEmulatedMachine(Guid clusterId, String emulatedMachine, boolean detectEmulatedMachine);

    /**
     * Sets the cluster's upgrade running flag to true.
     * @param clusterId The id of the cluster
     * @return flag indicating if updated succeeded
     */
    boolean setUpgradeRunning(Guid clusterId);

    /**
     * Clears the cluster's upgrade running flag.
     * @param clusterId The id of the cluster
     * @return flag indicating if updated succeeded
     */
    boolean clearUpgradeRunning(Guid clusterId);

    /**
     * Clears the cluster's upgrade running flag of all clusters.
     */
    void clearAllUpgradeRunning();

   /**
    * Retries trusted cluster
    */
    List<Cluster> getTrustedClusters();

    /**
     * Get all clusters attach to cluster policy
     */
    List<Cluster> getClustersByClusterPolicyId(Guid clusterPolicyId);

    /**
     * Get all clusters where currently no VM is migrating
     * @return list of clusters
     */
    List<Cluster> getWithoutMigratingVms();


    /**
     * Retrieves the number of the VMs in the cluster
     */
    int getVmsCountByClusterId(Guid clusterId);

    List<Cluster> getClustersByServiceAndCompatibilityVersion(boolean glusterService, boolean virtService, String compatibilityVersion);

    /**
     * @param macPoolId id of mac pool.
     * @return all Cluster records bound to given macPoolId.
     */
    List<Cluster> getAllClustersByMacPoolId(Guid macPoolId);


    /**
     * @param defaultNetworkProviderId id of defaultNetworkProvider.
     * @return all Cluster records using the given defaultNetworkProviderId.
     */
    List<Cluster> getAllClustersByDefaultNetworkProviderId(Guid defaultNetworkProviderId);

    /**
     * Retrieves the cluster id dedicated to vds name or vds address
     *
     * @param hostName the hostname
     * @param hostAddress ip address for the host
     * @return clusterId cluster id
     */
    Guid getClusterIdForHostByNameOrAddress(String hostName, String hostAddress);
}
