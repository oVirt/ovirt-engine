package org.ovirt.engine.core.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.storage_pool_iso_map;
import org.ovirt.engine.core.compat.Guid;

/**
 * StoragePoolIsoMap DAO hibernate implementation
 *
 */
public class StoragePoolIsoMapDAOHibernateImpl extends BaseDAOHibernateImpl<storage_pool_iso_map, StoragePoolIsoMapId> {

    public StoragePoolIsoMapDAOHibernateImpl() {
        super(storage_pool_iso_map.class);
    }

    /**
     * Gets all maps for a given storage pool ID
     * @param storagePoolId storage pool ID for which the maps will be returned
     * @return list of maps
     */
    @SuppressWarnings("unchecked")
    public List<storage_pool_iso_map> getAllForStoragePool(Guid storagePoolId) {
        Session session = getSession();
        Query query = session.getNamedQuery("all_storage_pool_iso_map_by_storage_pool_id");
        query.setParameter("storagePoolId", storagePoolId);
        return query.list();
    }

    /**
     * Gets all maps for a given storage ID
     * @param storageId storage ID to return all the maps for
     * @return list of maps (empty list if there is no matching map)
     */
    @SuppressWarnings("unchecked")
    public List<storage_pool_iso_map> getAllForStorage(Guid storageId) {
        Session session = getSession();
        Query query = session.getNamedQuery("all_storage_pool_iso_map_by_storage_id");
        query.setParameter("storageId", storageId);
        return query.list();
    }
}
