package org.ovirt.engine.core.dao;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import org.ovirt.engine.core.common.businessentities.LUN_storage_server_connection_map;
import org.ovirt.engine.core.common.businessentities.LUN_storage_server_connection_map_id;

public class StorageServerConnectionLunMapDAOHibernateImpl extends BaseDAOHibernateImpl<LUN_storage_server_connection_map, LUN_storage_server_connection_map_id>
        implements StorageServerConnectionLunMapDAO {
    public StorageServerConnectionLunMapDAOHibernateImpl() {
        super(LUN_storage_server_connection_map.class);
    }

    public List<LUN_storage_server_connection_map> getAll(String lunId) {
        return findByCriteria(Restrictions.eq("id.lunId", lunId));
    }
}
