package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.storage_pool_iso_map;
import org.ovirt.engine.core.compat.Guid;

/**
 * StoragePoolIsoMap DAO
 *
 */
public interface StoragePoolIsoMapDAO extends GenericDao<storage_pool_iso_map, StoragePoolIsoMapId>,
        StatusAwareDao<StoragePoolIsoMapId, StorageDomainStatus> {
    /**
     * Gets all maps for a given storage pool ID
     *
     * @param storagePoolId
     *            storage pool ID for which the maps will be returned
     * @return list of maps
     */
    public List<storage_pool_iso_map> getAllForStoragePool(Guid storagePoolId);

    /**
     * Gets all maps for a given storage ID
     *
     * @param storageId
     *            storage ID to return all the maps for
     * @return list of maps (empty list if there is no matching map)
     */
    public List<storage_pool_iso_map> getAllForStorage(Guid storageId);
}
