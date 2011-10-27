package org.ovirt.engine.core.dao.users;

import org.ovirt.engine.core.dal.dbbroker.user_sessions;
import org.ovirt.engine.core.dal.dbbroker.user_sessions_id;
import org.ovirt.engine.core.dao.BaseDAOHibernateImpl;

public class SessionDAOHibernateImpl extends BaseDAOHibernateImpl<user_sessions, user_sessions_id> {
    public SessionDAOHibernateImpl() {
        super(user_sessions.class);
    }
}
