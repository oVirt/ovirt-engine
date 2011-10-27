package org.ovirt.engine.core.dao;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import org.ovirt.engine.core.common.businessentities.image_group_storage_domain_map;
import org.ovirt.engine.core.common.businessentities.image_group_storage_domain_map_id;
import org.ovirt.engine.core.compat.Guid;

public class ImageGroupStorageDomainMapDAOHibernateImpl extends BaseDAOHibernateImpl<image_group_storage_domain_map, image_group_storage_domain_map_id> {
    public ImageGroupStorageDomainMapDAOHibernateImpl() {
        super(image_group_storage_domain_map.class);
    }

    public List<image_group_storage_domain_map> getAllForImage(Guid image_group_id) {
        return findByCriteria(Restrictions.eq("id.imageGroupId", image_group_id));
    }

    public List<image_group_storage_domain_map> findAllForStorageDomain(Guid storage_domain_id) {
        return findByCriteria(Restrictions.eq("id.storageDomainId", storage_domain_id));
    }

}
