package org.ovirt.engine.core.dao.events;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import org.ovirt.engine.core.common.businessentities.event_subscriber;
import org.ovirt.engine.core.common.businessentities.event_subscriber_id;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDAOHibernateImpl;

public class SubscriberDAOHibernateImpl extends BaseDAOHibernateImpl<event_subscriber, event_subscriber_id> {
    public SubscriberDAOHibernateImpl() {
        super(event_subscriber.class);
    }

    public List<event_subscriber> getAllForSubscriber(Guid id) {
        return findByCriteria(Restrictions.eq("id.subscriberId", id));
    }

    public void update(event_subscriber subscriber, int oldMethodId) {
        /*
         * for some odd reason doing a simple update on an event_subscriber object doesn't update the object, so an
         * explicit HQL update is necessary
         */
        Session session = getSession();
        Transaction transaction = session.beginTransaction();

        Query query = session.createQuery("update event_subscriber es " +
                "set es.id.methodId = :method_id " +
                "where es.id.eventUpName = :event_up_name " +
                "and es.id.methodId = :old_method_id " +
                "and es.id.subscriberId = :subscriber_id");

        query.setParameter("method_id", subscriber.getmethod_id());
        query.setParameter("event_up_name", subscriber.getevent_up_name());
        query.setParameter("old_method_id", oldMethodId);
        query.setParameter("subscriber_id", subscriber.getsubscriber_id());

        query.executeUpdate();

        transaction.commit();
    }
}
