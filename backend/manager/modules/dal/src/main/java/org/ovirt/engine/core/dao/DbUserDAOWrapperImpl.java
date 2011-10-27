package org.ovirt.engine.core.dao;

import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.user_sessions;
import org.ovirt.engine.core.dal.dbbroker.user_sessions_id;
import org.ovirt.engine.core.dao.users.SessionDAOHibernateImpl;
import org.ovirt.engine.core.dao.users.UserDAOHibernateImpl;

/**
 * <code>DbUserDAOWrapperImpl</code> provides an implementation of {@link DbUserDAO} using child DAOs.
 *
 */
public class DbUserDAOWrapperImpl extends BaseDAOWrapperImpl implements DbUserDAO {
    private UserDAOHibernateImpl userDAO = new UserDAOHibernateImpl();
    private SessionDAOHibernateImpl sessionDAO = new SessionDAOHibernateImpl();

    @Override
    public void setSession(Session session) {
        super.setSession(session);

        userDAO.setSession(session);
        sessionDAO.setSession(session);
    }

    @Override
    public DbUser get(Guid id) {
        return userDAO.get(id);
    }

    @Override
    public DbUser getByUsername(String username) {
        return userDAO.findOneByCriteria(Restrictions.eq("username", username));
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DbUser> getAllForVm(Guid id) {
        Session session = getSession();
        Query query = session.createQuery("select user from DbUser user, permissions perms " +
                "where user.id = perms.adElementId " +
                "and perms.objectType = 2 " +
                "and perms.objectId = :vm_id");

        query.setParameter("vm_id", id);

        return (List<DbUser>) query.list();
    }

    @Override
    public List<DbUser> getAllTimeLeasedUsersForVm(int vmid) {
        // TODO this API is broken
        return null;
    }

    @Override
    public List<DbUser> getAllWithQuery(String query) {
        return userDAO.findAllWithSQL(query);
    }

    @Override
    public List<DbUser> getAll() {
        return userDAO.getAll();
    }

    @Override
    public List<user_sessions> getAllUserSessions() {
        return sessionDAO.getAll();
    }

    @Override
    public void save(DbUser user) {
        userDAO.save(user);
    }

    @Override
    public void saveSession(user_sessions session) {
        sessionDAO.save(session);
    }

    @Override
    public void update(DbUser user) {
        save(user);
    }

    @Override
    public void remove(Guid user) {
        userDAO.remove(user);
    }

    @Override
    public void removeUserSession(String session, Guid user) {
        sessionDAO.remove(new user_sessions_id(user, session));
    }

    @Override
    public void removeUserSessions(Map<String, Guid> sessionmap) {
        for (Map.Entry<String, Guid> entry : sessionmap.entrySet()) {
            sessionDAO.remove(new user_sessions_id(entry.getValue(), entry.getKey()));
        }
    }

    @Override
    public void removeAllSessions() {
        // List<user_sessions> all = sessionDAO.getAll();
        // user_sessions[] sessions = new user_sessions[all.size()];
        // all.toArray(sessions);

        for (user_sessions session : sessionDAO.getAll()) {
            sessionDAO.remove(new user_sessions_id(session.getuser_id(), session.getsession_id()));
        }
    }
}
