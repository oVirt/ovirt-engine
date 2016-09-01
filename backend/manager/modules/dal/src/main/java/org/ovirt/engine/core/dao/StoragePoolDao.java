package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.compat.Guid;

/**
 * {@code StoragePoolDao} defines a type for performing CRUD operations on instances of {@link StoragePool}.
 */
public interface StoragePoolDao extends GenericDao<StoragePool, Guid>, StatusAwareDao<Guid, StoragePoolStatus>, SearchDao<StoragePool> {

    /**
     * Increase master_domain_version of storage pool in DB and return a new value
     * @param id
     *            The id of storage pool
     * @return The new value of master domain version
     */
    int increaseStoragePoolMasterVersion(Guid id);

    /**
     * Retrieves the storage pool with the given ID, with optional filtering
     * @param ID
     *            The ID of the storage pool
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     * @return The storage pool
     */
    StoragePool get(Guid ID, Guid userID, boolean isFiltered);

    /**
     * Retrieves the storage pool with the given name.
     *
     * @param name
     *            the storage pool name (case-sensitive get)
     * @return the storage pool
     */
    StoragePool getByName(String name);

    /**
     * Retrieves the storage pools with the given name.
     *
     * @param name
     *            the storage pool name
     * @param isCaseSensitive
     *            whether to do case-sensitive get or not
     * @return the storage pool
     */
    List<StoragePool> getByName(String name, boolean isCaseSensitive);

    /**
     * Retrieves the storage pool for the specified VDS.
     *
     * @param vds
     *            the VDS
     * @return the storage pool
     */
    StoragePool getForVds(Guid vds);

    /**
     * Retrieves the storage pool for the specified VDS group.
     *
     * @param cluster
     *            the VDS group
     * @return the storage pool
     */
    StoragePool getForCluster(Guid cluster);

    /**
     *  @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     * @return The list of storage pool
     */
    List<StoragePool> getAll(Guid userID, boolean isFiltered);


    /**
     * @param status
     *            the status of the wanted storage pools
     * @return The list of storage pools with the given status
     */
    List<StoragePool> getAllByStatus(StoragePoolStatus status);

    /**
     * Retrieves all storage pools for the given storage domain.
     *
     * @param storageDomain
     *            the storage domain
     * @return the list of storage pools
     */
    List<StoragePool> getAllForStorageDomain(Guid storageDomain);

    /**
     * The following method should update only part of storage pool. It will update only name, description
     * and compatibility version fields
     */
    void updatePartial(StoragePool pool);

    /**
     * Retrieves data centers containing clusters with permissions to perform the given action.
     * @return list of data centers
     */
    List<StoragePool> getDataCentersWithPermittedActionOnClusters(Guid userId, ActionGroup actionGroup, boolean supportsVirtService, boolean supportsGlusterService);

    /**
     * Retrieves data centers with at least one cluster with virt service specified
     *
     * @return list of data centers
     */
    List<StoragePool> getDataCentersByClusterService(boolean supportsVirtService, boolean supportsGlusterService);

    /**
     * Retrieves the IDs of all data centers to which an external network has been imported.
     *
     * @param externalId
     *            the external network's external ID.
     * @return the list of data center IDs.
     */
    List<Guid> getDcIdByExternalNetworkId(String externalId);
}
