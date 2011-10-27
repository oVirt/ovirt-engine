package org.ovirt.engine.core.dao.tags;

import org.ovirt.engine.core.common.businessentities.tags_vds_map;
import org.ovirt.engine.core.common.businessentities.tags_vds_map_id;
import org.ovirt.engine.core.dao.BaseDAOHibernateImpl;

public class TagVdsMapDAO extends BaseDAOHibernateImpl<tags_vds_map, tags_vds_map_id> {
    public TagVdsMapDAO() {
        super(tags_vds_map.class);
    }
}
