package org.ovirt.engine.core.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>AdGroupDAOHibernateImpl</code> provides an implementation of {@link AdGroupDAO} that uses Hibernate for
 * underlying persistence.
 *
 */
public class AdGroupDAOHibernateImpl extends BaseDAOHibernateImpl<LdapGroup, Guid> implements AdGroupDAO {
    @Override
    public void remove(Guid id) {
        /*
         * This implementation was necessary in order to handle cascading deletes of what is in the tags_user_group_map
         * table. it can be moved to the default generic implementation when the association between ad_groups and tags
         * are handled via @OneToMany relationships.
         */
        Session session = getSession();

        Transaction transaction = session.beginTransaction();

        Query query = session.createQuery("delete from tags_user_group_map tugm where tugm.groupId = :group_id");

        query.setParameter("group_id", id);
        query.executeUpdate();

        LdapGroup instance = get(id);

        if (instance != null) {
            session.delete(instance);
        }

        transaction.commit();
    }

    public AdGroupDAOHibernateImpl() {
        super(LdapGroup.class);
    }

    @Override
    public List<LdapGroup> getAllTimeLeasedForPool(int id) {
        // TODO Auto-generated method stub
        return null;
    }
}
