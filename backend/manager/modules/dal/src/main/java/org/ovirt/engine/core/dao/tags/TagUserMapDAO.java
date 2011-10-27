package org.ovirt.engine.core.dao.tags;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import org.ovirt.engine.core.common.businessentities.tags_user_map;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDAOHibernateImpl;

public class TagUserMapDAO extends BaseDAOHibernateImpl<tags_user_map, Guid> {
    public TagUserMapDAO() {
        super(tags_user_map.class);
    }

    @SuppressWarnings("unchecked")
    public List<tags_user_map> getTaguserMapByTagName(String tagName) {
        Session session = getSession();
        Query query = session.getNamedQuery("all_tag_user_maps_by_tag_name");

        query.setParameter("tag_name", tagName);

        return query.list();
    }

    public void remove(Guid tagId, Guid userId) {
        Session session = getSession();
        Query query = session.createQuery("delete tags_user_map where tagId = :tag_id and userId = :user_id");

        query.setParameter("tag_id", tagId);
        query.setParameter("user_id", userId);

        session.beginTransaction();
        query.executeUpdate();
        session.getTransaction().commit();
    }
}
