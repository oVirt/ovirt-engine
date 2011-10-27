package org.ovirt.engine.core.dao.images;

import org.ovirt.engine.core.common.businessentities.DiskImageDynamic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDAOHibernateImpl;

public class DiskImageDynamicDAOHibernateImpl extends BaseDAOHibernateImpl<DiskImageDynamic, Guid> {
    public DiskImageDynamicDAOHibernateImpl() {
        super(DiskImageDynamic.class);
    }
}
