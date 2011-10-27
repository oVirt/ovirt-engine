package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.businessentities.storage_domain_dynamic;
import org.ovirt.engine.core.compat.Guid;

public class StorageDomainDynamicDAOHibernateImpl extends BaseDAOHibernateImpl<storage_domain_dynamic, Guid> implements StorageDomainDynamicDAO {
    public StorageDomainDynamicDAOHibernateImpl() {
        super(storage_domain_dynamic.class);
    }
}
