package org.ovirt.engine.core.dao.images;

import org.ovirt.engine.core.common.businessentities.image_vm_map;
import org.ovirt.engine.core.common.businessentities.image_vm_map_id;
import org.ovirt.engine.core.dao.BaseDAOHibernateImpl;

public class ImageVmMapDAOHibernateImpl extends BaseDAOHibernateImpl<image_vm_map, image_vm_map_id> {
    public ImageVmMapDAOHibernateImpl() {
        super(image_vm_map.class);
    }
}
