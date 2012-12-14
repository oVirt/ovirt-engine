package org.ovirt.engine.core.dao.tags;

import org.ovirt.engine.core.common.businessentities.TagsVdsMap;
import org.ovirt.engine.core.common.businessentities.TagsVdsMapId;
import org.ovirt.engine.core.dao.BaseDAOHibernateImpl;

public class TagVdsMapDAO extends BaseDAOHibernateImpl<TagsVdsMap, TagsVdsMapId> {
    public TagVdsMapDAO() {
        super(TagsVdsMap.class);
    }
}
