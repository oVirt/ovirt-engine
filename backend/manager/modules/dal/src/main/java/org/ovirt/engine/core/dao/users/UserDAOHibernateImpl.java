package org.ovirt.engine.core.dao.users;

import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDAOHibernateImpl;

public class UserDAOHibernateImpl extends BaseDAOHibernateImpl<DbUser, Guid> {
    public UserDAOHibernateImpl() {
        super(DbUser.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DbUser> findAllWithSQL(String sql) {
        Session session = getSession();
        SQLQuery query = session.createSQLQuery(sql).addEntity(DbUser.class);

        return (List<DbUser>) query.list();
    }
}
