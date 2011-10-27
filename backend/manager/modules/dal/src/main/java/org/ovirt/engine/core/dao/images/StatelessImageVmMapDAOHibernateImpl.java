package org.ovirt.engine.core.dao.images;

import org.ovirt.engine.core.common.businessentities.stateless_vm_image_map;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDAOHibernateImpl;

public class StatelessImageVmMapDAOHibernateImpl extends BaseDAOHibernateImpl<stateless_vm_image_map, Guid> {
    public StatelessImageVmMapDAOHibernateImpl() {
        super(stateless_vm_image_map.class);
    }
}
