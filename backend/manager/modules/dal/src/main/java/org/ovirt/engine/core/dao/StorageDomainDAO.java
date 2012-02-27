package org.ovirt.engine.core.dao;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.image_storage_domain_map;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

/**
 * <code>StorageDomainDAO</code> defines a type for performing CRUD operations on instances of {@link storage_domains}.
 *
 *
 */
public interface StorageDomainDAO extends DAO, SearchDAO<storage_domains> {
    /**
     * Retrieves the master storage domain for the specified pool.
     *
     * @param pool
     *            the storage pool
     * @return the master storage domain
     */
    Guid getMasterStorageDomainIdForPool(Guid pool);

    /**
     * Retrieves the master storage domain for the specified pool.
     *
     * @param pool
     *            the storage pool
     * @return the master storage domain
     */
    Guid getIsoStorageDomainIdForPool(Guid pool);

    /**
     * Retrieves the storage domain with specified id.
     *
     * @param id
     *            the storage domain id
     * @return the storage domain
     */
    storage_domains get(Guid id);

    /**
     * Retrieves the storage domain with specified id, with optional permissions filtering.
     *
     * @param id
     *            the storage domain id
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     * @return the storage domain
     */
    storage_domains get(Guid id, Guid userID, boolean isFiltered);

    /**
     * Retrieves the storage domain for the given pool with the specified id.
     *
     * @param id
     *            the storage domain id
     * @param storagepool
     *            the storage pool
     * @return the storage domain
     */
    storage_domains getForStoragePool(Guid id, NGuid storagepool);

    /**
     * Retrieves all storage domains for the specified connection.
     *
     * @param connection
     *            The connection
     * @return the list of storage domains (empty if no storage is using this connection)
     */
    List<storage_domains> getAllForConnection(String connection);

    /**
     * Retrieves all storage domains for the specified storage pool.
     *
     * @param pool
     *            the storage pool
     * @return the list of storage domains
     */
    List<storage_domains> getAllForStoragePool(Guid pool);

    /**
     * Retrieves all storage domains for the specified storage pool, with optional filtering.
     *
     * @param pool
     *            the storage pool
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     * @return the list of storage domains
     */
    List<storage_domains> getAllForStoragePool(Guid pool, Guid userID, boolean isFiltered);

    /**
     * Retrieves all storage domains for the specified storage domain id.
     *
     * @param id
     *            the storage domain id
     * @return the list of storage domains
     */
    List<storage_domains> getAllForStorageDomain(Guid id);

    /**
     * Retrieves all storage domains.
     *
     * @return the list of storage domains
     */
    List<storage_domains> getAll();

    /**
     * Retrieves all domain which contains any image from provided group
     *
     * @param imageGroupId
     * @return
     */
    List<Guid> getAllStorageDomainsByImageGroup(Guid imageGroupId);

    /**
     * Removes the specified storage domain.
     *
     * @param id
     *            the storage domain
     */
    void remove(Guid id);

    void addImageStorageDomainMap(image_storage_domain_map image_group_storage_domain_map);

    void removeImageStorageDomainMap(image_storage_domain_map image_group_storage_domain_map);

    void removeImageStorageDomainMap(Guid image_id);

    List<image_storage_domain_map> getAllImageStorageDomainMapsForStorageDomain(
            Guid storage_domain_id);

    List<image_storage_domain_map> getAllImageStorageDomainMapsForImage(
            Guid image_id);

    ArrayList<Guid> getAllImageStorageDomainIdsForImage(
            Guid image_id);

    /**
     * Retrieves all storage domains for the specified connection.
     * @param storagePoolId
     *            The storage pool id
     * @param connection
     *            The connection
     * @return the list of storage domains (empty if no storage is using this connection)
     */
    List<storage_domains> getAllByStoragePoolAndConnection(Guid storagePoolId, String connection);

}
