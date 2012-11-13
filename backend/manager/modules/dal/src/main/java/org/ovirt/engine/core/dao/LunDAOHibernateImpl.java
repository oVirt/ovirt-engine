package org.ovirt.engine.core.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;

import org.ovirt.engine.core.common.businessentities.LUNs;

public class LunDAOHibernateImpl extends BaseDAOHibernateImpl<LUNs, String> implements LunDAO {
    public LunDAOHibernateImpl() {
        super(LUNs.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<LUNs> getAllForStorageServerConnection(String id) {
        Query query = getSession().getNamedQuery("all_luns_for_storage_server_connection");

        query.setParameter("storage_server_connection", id);

        return query.list();
    }

    @Override
    public List<LUNs> getAllForVolumeGroup(String id) {
        return findByCriteria(Restrictions.eq("volumeGroupId", id));
    }

    @Override
    public void updateLUNsVolumeGroupId(String id, String volumeGroupId) {
        // TODO Auto-generated method stub

    }
}
