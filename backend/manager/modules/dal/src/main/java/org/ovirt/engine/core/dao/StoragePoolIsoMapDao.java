package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.compat.Guid;

public interface StoragePoolIsoMapDao extends GenericDao<StoragePoolIsoMap, StoragePoolIsoMapId>,
        StatusAwareDao<StoragePoolIsoMapId, StorageDomainStatus> {
    /**
     * Gets all maps for a given storage pool ID
     *
     * @param storagePoolId
     *            storage pool ID for which the maps will be returned
     * @return list of maps
     */
    public List<StoragePoolIsoMap> getAllForStoragePool(Guid storagePoolId);

    /**
     * Gets all maps for a given storage ID
     *
     * @param storageId
     *            storage ID to return all the maps for
     * @return list of maps (empty list if there is no matching map)
     */
    public List<StoragePoolIsoMap> getAllForStorage(Guid storageId);
}
