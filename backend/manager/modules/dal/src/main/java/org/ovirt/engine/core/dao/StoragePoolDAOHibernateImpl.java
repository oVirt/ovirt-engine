package org.ovirt.engine.core.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.Guid;

public class StoragePoolDAOHibernateImpl extends BaseDAOHibernateImpl<storage_pool, Guid> implements StoragePoolDAO {

   // private StoragePoolIsoMapDAO storagePoolIsoMapDAO = new StoragePoolIsoMapDAO();

    public StoragePoolDAOHibernateImpl() {
        super(storage_pool.class);
    }

    @Override
    public void setSession(Session session) {
        super.setSession(session);
        //storagePoolIsoMapDAO.setSession(session);
    }

    @Override
    public storage_pool getForVds(final Guid vds) {
        Session session = getSession();
        Query query = session.getNamedQuery("all_storage_pools_for_vds");
        query.setParameter("vds_id", vds);
        return (storage_pool) query.uniqueResult();
    }

    @Override
    public storage_pool getForVdsGroup(final Guid vdsGroup) {
        Session session = getSession();
        Query query = session.getNamedQuery("all_storage_pools_for_vds_group");

        query.setParameter("vds_group_id", vdsGroup);

        return (storage_pool) query.uniqueResult();
    }

    @Override
    public List<storage_pool> getAllOfType(final StorageType type) {
        return findByCriteria(Restrictions.eq("storagePoolType", type.getValue()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<storage_pool> getAllForStorageDomain(final Guid storageDomain) {
        Session session = getSession();
        Query query = session.getNamedQuery("all_storage_pools_for_storage_domain");

        query.setParameter("storage_domain_id", storageDomain);

        return query.list();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<storage_pool> getAllWithQuery(final String queryString) {
        Session session = getSession();
        Query query = session.createQuery(queryString);

        return query.list();
    }

    @Override
    public void updateStatus(Guid id, StoragePoolStatus status) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updatePartial(storage_pool pool) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<storage_pool> getDataCentersWithPermittedActionOnClusters(Guid userId, ActionGroup actionGroup) {
        Query query = getSession().getNamedQuery("fn_perms_get_storage_pools_with_permitted_action_on_vds_groups");

        query.setParameter("v_user_id", userId).setParameter("v_action_group_id", actionGroup.getId());

        return (List<storage_pool>) query.uniqueResult();
    }

    /* Moved to iso map dao , left modifications to Derril
    @Override
    public storage_pool_iso_map getStoragePoolIsoMapByIsoAndStoragePool(Guid isoId, Guid storagePoolId) {
        return storagePoolIsoMapDAO.get(new StoragePoolIsoMapId(isoId,storagePoolId));
    }

    @Override
    public void addStoragePoolIsoMap(storage_pool_iso_map map) {
        storagePoolIsoMapDAO.save(map);

    }

    @Override
    public void updateStoragePoolIsoMap(storage_pool_iso_map map) {
        storagePoolIsoMapDAO.update(map);
    }

    @Override
    public void removeStoragePoolIsoMap(Guid storagePoolId, Guid isoDomainId) {
        storagePoolIsoMapDAO.remove(new StoragePoolIsoMapId(isoDomainId,storagePoolId));
    }

    @Override
    public List<storage_pool_iso_map> getAllStoragePoolIsoMapsForStoragePool(Guid storagePoolId) {
        return storagePoolIsoMapDAO.getAllForStoragePool(storagePoolId);
    }

    @Override
    public List<storage_pool_iso_map> getAllStoragePoolIsoMapsForIso(Guid isoId) {
        return storagePoolIsoMapDAO.getAllForStorage(isoId);

    }
    */
}
