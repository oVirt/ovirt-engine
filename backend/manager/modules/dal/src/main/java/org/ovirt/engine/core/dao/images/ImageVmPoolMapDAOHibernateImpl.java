package org.ovirt.engine.core.dao.images;

import org.ovirt.engine.core.common.businessentities.image_vm_pool_map;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDAOHibernateImpl;

public class ImageVmPoolMapDAOHibernateImpl extends BaseDAOHibernateImpl<image_vm_pool_map, Guid> {
    public ImageVmPoolMapDAOHibernateImpl() {
        super(image_vm_pool_map.class);
    }
}
