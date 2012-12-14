package org.ovirt.engine.core.dao;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

public class StorageDomainStaticDAOHibernateImpl extends BaseDAOHibernateImpl<StorageDomainStatic, Guid> implements StorageDomainStaticDAO {

    public StorageDomainStaticDAOHibernateImpl() {
        super(StorageDomainStatic.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<StorageDomainStatic> getAllForStoragePoolOfStorageType(StorageType type, Guid pool) {
        Query query = getSession().getNamedQuery("all_storage_domain_static_for_storage_pool_of_type");

        query.setParameter("pool_id", pool);
        query.setParameter("storage_type", type);

        return fillInDetails(query.list());
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<StorageDomainStatic> getAllForStoragePool(Guid pool) {
        Query query = getSession().getNamedQuery("all_storage_domain_static_for_storage_pool");

        query.setParameter("pool_id", pool);

        return fillInDetails(query.list());
    }

    private List<StorageDomainStatic> fillInDetails(List<StorageDomainStatic> list) {
        for (StorageDomainStatic sds : list) {
            Query query =
                    getSession().createQuery("select pool.name from storage_pool pool, storage_pool_iso_map map " +
                            "where pool.id = map.id.storagePoolId " +
                            "and map.id.storageId = :storage_id");

            query.setParameter("storage_id", sds.getId());

            @SuppressWarnings("unchecked")
            List<String> names = query.list();
            sds.setstorage_pool_name(names.isEmpty() != true ? names.get(0) : "");
        }
        return list;
    }

    @Override
    public List<StorageDomainStatic> getAllOfStorageType(StorageType type) {
        List<StorageDomainStatic> result = findByCriteria(Restrictions.eq("storagePoolType", type));

        return fillInDetails(result);
    }

    public StorageDomainStatic getForStoragePool(Guid id, NGuid storagepool) {
        Query query = getSession().getNamedQuery("storage_domain_static_for_storage_pool");

        query.setParameter("id", id);
        query.setParameter("storage_pool_id", storagepool);

        return (StorageDomainStatic) query.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<StorageDomainStatic> getAllForImageGroup(NGuid imageGroup) {
        Query query = getSession().getNamedQuery("all_storage_domain_static_for_image_group");

        query.setParameter("image_group_id", imageGroup);

        return query.list();
    }

    @SuppressWarnings("unchecked")
    public List<StorageDomainStatic> getAllForStorageDomain(Guid id) {
        Query query = getSession().getNamedQuery("all_storage_domain_static_for_storage_domain");

        query.setParameter("storage_domain_id", id);

        return query.list();
    }

    @Override
    public List<Guid> getAllIds(Guid pool, StorageDomainStatus status) {
        // TODO Auto-generated method stub
        throw new NotImplementedException();
    }
}
