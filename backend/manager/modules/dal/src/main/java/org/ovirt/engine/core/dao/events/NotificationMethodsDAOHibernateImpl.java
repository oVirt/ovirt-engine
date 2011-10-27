package org.ovirt.engine.core.dao.events;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import org.ovirt.engine.core.common.businessentities.event_notification_methods;
import org.ovirt.engine.core.dao.BaseDAOHibernateImpl;

public class NotificationMethodsDAOHibernateImpl extends BaseDAOHibernateImpl<event_notification_methods, Integer> {
    public NotificationMethodsDAOHibernateImpl() {
        super(event_notification_methods.class);
    }

    public List<event_notification_methods> getEventNotificationMethodsById(int method_id) {
        return findByCriteria(Restrictions.eq("methodId", method_id));
    }

    public List<event_notification_methods> getEventNotificationMethodsByType(String method_type) {
        return findByCriteria(Restrictions.eq("methodType", method_type));
    }
}
