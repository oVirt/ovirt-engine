package org.ovirt.engine.core.dao;

import org.hibernate.Session;

/**
 * <code>BaseDAOHibernateImpl</code> captures the common functions for DAOs that wrap other DAOs.
 *
 */
public class BaseDAOWrapperImpl {
    private static ThreadLocal<Session> sessions = new ThreadLocal<Session>();

    public void setSession(Session session) {
        sessions.set(session);
    }

    protected Session getSession() {
        return sessions.get();
    }
}
