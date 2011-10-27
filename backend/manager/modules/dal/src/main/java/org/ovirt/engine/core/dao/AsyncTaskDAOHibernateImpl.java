package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.businessentities.async_tasks;
import org.ovirt.engine.core.compat.Guid;

public class AsyncTaskDAOHibernateImpl extends BaseDAOHibernateImpl<async_tasks, Guid> implements AsyncTaskDAO {
    public AsyncTaskDAOHibernateImpl() {
        super(async_tasks.class);
    }

}
