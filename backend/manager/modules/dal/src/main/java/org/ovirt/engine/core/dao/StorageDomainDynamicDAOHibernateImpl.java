package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.businessentities.StorageDomainDynamic;
import org.ovirt.engine.core.compat.Guid;

public class StorageDomainDynamicDAOHibernateImpl extends BaseDAOHibernateImpl<StorageDomainDynamic, Guid> implements StorageDomainDynamicDAO {
    public StorageDomainDynamicDAOHibernateImpl() {
        super(StorageDomainDynamic.class);
    }
}
