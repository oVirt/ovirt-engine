package org.ovirt.engine.core.dao.events;

import org.hibernate.criterion.Restrictions;

import org.ovirt.engine.core.common.businessentities.event_notification_hist;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDAOHibernateImpl;

public class NotificationHistoryDAOHibernateImpl extends BaseDAOHibernateImpl<event_notification_hist, Guid> {
    public NotificationHistoryDAOHibernateImpl() {
        super(event_notification_hist.class);
    }

    public event_notification_hist getHistory(Guid subscriberId, long auditLogId) {
        return findOneByCriteria(Restrictions.eq("subscriberId", subscriberId),
                Restrictions.eq("auditLogId", auditLogId));
    }
}
