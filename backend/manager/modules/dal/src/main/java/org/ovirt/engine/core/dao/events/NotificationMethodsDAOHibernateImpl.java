package org.ovirt.engine.core.dao.events;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import org.ovirt.engine.core.common.businessentities.EventNotificationMethod;
import org.ovirt.engine.core.dao.BaseDAOHibernateImpl;

public class NotificationMethodsDAOHibernateImpl extends BaseDAOHibernateImpl<EventNotificationMethod, Integer> {
    public NotificationMethodsDAOHibernateImpl() {
        super(EventNotificationMethod.class);
    }

    public List<EventNotificationMethod> getEventNotificationMethodsById(int method_id) {
        return findByCriteria(Restrictions.eq("methodId", method_id));
    }

    public List<EventNotificationMethod> getEventNotificationMethodsByType(String method_type) {
        return findByCriteria(Restrictions.eq("methodType", method_type));
    }
}
