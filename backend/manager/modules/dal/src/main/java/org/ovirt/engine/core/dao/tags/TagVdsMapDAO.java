package org.ovirt.engine.core.dao.tags;

import org.ovirt.engine.core.common.businessentities.tags_vds_map;
import org.ovirt.engine.core.common.businessentities.TagsVdsMapId;
import org.ovirt.engine.core.dao.BaseDAOHibernateImpl;

public class TagVdsMapDAO extends BaseDAOHibernateImpl<tags_vds_map, TagsVdsMapId> {
    public TagVdsMapDAO() {
        super(tags_vds_map.class);
    }
}
