package org.ovirt.engine.core.dao.tags;

import org.hibernate.Query;
import org.hibernate.Session;

import org.ovirt.engine.core.common.businessentities.TagsVmMap;
import org.ovirt.engine.core.common.businessentities.TagsVmMapId;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDAOHibernateImpl;

public class TagVmMapDAO extends BaseDAOHibernateImpl<TagsVmMap, TagsVmMapId> {
    public TagVmMapDAO() {
        super(TagsVmMap.class);
    }

    public void remove(Guid tagId, Guid vmId) {
        Session session = getSession();
        Query query =
                session.createQuery("delete tags_vm_map m where m.id.tagId = :tag_id and m.id.vmId = :vm_id");

        query.setParameter("tag_id", tagId);
        query.setParameter("vm_id", vmId);

        session.beginTransaction();
        query.executeUpdate();
        session.getTransaction().commit();
    }
}
