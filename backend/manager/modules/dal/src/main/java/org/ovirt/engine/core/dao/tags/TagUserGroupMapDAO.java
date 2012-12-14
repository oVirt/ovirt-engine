package org.ovirt.engine.core.dao.tags;

import org.hibernate.Query;
import org.hibernate.Session;

import org.ovirt.engine.core.common.businessentities.TagsUserGroupMap;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDAOHibernateImpl;

public class TagUserGroupMapDAO extends BaseDAOHibernateImpl<TagsUserGroupMap, Guid> {
    public TagUserGroupMapDAO() {
        super(TagsUserGroupMap.class);
    }

    public void remove(Guid tagId, Guid groupId) {
        Session session = getSession();
        Query query =
                session.createQuery("delete tags_user_group_map where tagId = :tag_id and groupId = :group_id");

        query.setParameter("tag_id", tagId);
        query.setParameter("group_id", groupId);

        session.beginTransaction();
        query.executeUpdate();
        session.getTransaction().commit();
    }
}
