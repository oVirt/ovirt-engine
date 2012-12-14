package org.ovirt.engine.core.dao;

import java.util.List;

import org.hibernate.Session;

import org.ovirt.engine.core.common.businessentities.event_map;
import org.ovirt.engine.core.common.businessentities.event_notification_hist;
import org.ovirt.engine.core.common.businessentities.EventNotificationMethod;
import org.ovirt.engine.core.common.businessentities.event_subscriber;
import org.ovirt.engine.core.common.businessentities.event_subscriber_id;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.events.MapDAOHibernateImpl;
import org.ovirt.engine.core.dao.events.NotificationHistoryDAOHibernateImpl;
import org.ovirt.engine.core.dao.events.NotificationMethodsDAOHibernateImpl;
import org.ovirt.engine.core.dao.events.SubscriberDAOHibernateImpl;

public class EventDAOWrapperImpl extends BaseDAOWrapperImpl implements EventDAO {
    private MapDAOHibernateImpl mapDAO = new MapDAOHibernateImpl();
    private NotificationHistoryDAOHibernateImpl notificationHistoryDAO = new NotificationHistoryDAOHibernateImpl();
    private NotificationMethodsDAOHibernateImpl notificationMethodsDAO = new NotificationMethodsDAOHibernateImpl();
    private SubscriberDAOHibernateImpl subscriberDAO = new SubscriberDAOHibernateImpl();

    @Override
    public void setSession(Session session) {
        super.setSession(session);

        mapDAO.setSession(session);
        notificationHistoryDAO.setSession(session);
        notificationMethodsDAO.setSession(session);
        subscriberDAO.setSession(session);
    }

    @Override
    public List<event_subscriber> getAll() {
        return subscriberDAO.getAll();
    }

    @Override
    public List<event_subscriber> getAllForSubscriber(Guid id) {
        return subscriberDAO.getAllForSubscriber(id);
    }

    @Override
    public List<EventNotificationMethod> getAllEventNotificationMethods() {
        return notificationMethodsDAO.getAll();
    }

    @Override
    public List<EventNotificationMethod> getEventNotificationMethodsById(int method_id) {
        return notificationMethodsDAO.getEventNotificationMethodsById(method_id);
    }

    @Override
    public List<EventNotificationMethod> getEventNotificationMethodsByType(String method_type) {
        return notificationMethodsDAO.getEventNotificationMethodsByType(method_type);
    }

    @Override
    public void subscribe(event_subscriber subscriber) {
        subscriberDAO.save(subscriber);
    }

    @Override
    public void update(event_subscriber subscriber, int oldMethodId) {
        subscriberDAO.update(subscriber, oldMethodId);
    }

    @Override
    public void unsubscribe(event_subscriber subscriber) {
        subscriberDAO.remove(new event_subscriber_id(subscriber.getsubscriber_id(),
                subscriber.getevent_up_name(),
                subscriber.getmethod_id(),
                subscriber.gettag_name()));
    }

    @Override
    public List<event_map> getEventMapByName(String event_up_name) {
        return mapDAO.getByEventUpName(event_up_name);
    }

    @Override
    public List<event_map> getAllEventMaps() {
        return mapDAO.getAll();
    }

    public event_notification_hist getHistory(Guid subscriberId, long auditLogId) {
        return notificationHistoryDAO.getHistory(subscriberId, auditLogId);
    }
}
