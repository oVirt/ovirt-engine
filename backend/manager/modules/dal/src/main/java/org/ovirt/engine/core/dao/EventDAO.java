package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.EventMap;
import org.ovirt.engine.core.common.businessentities.EventNotificationMethod;
import org.ovirt.engine.core.common.businessentities.event_subscriber;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>EventDAO</code> defines a type for performing CRUD operations on instances of {@link event_subscriber}.
 *
 *
 */
public interface EventDAO extends DAO {
    /**
     * Retrieves all event subscriptions for the given subscriber id.
     *
     * @param id
     *            the subscriber id
     * @return the subscriptions
     */
    List<event_subscriber> getAllForSubscriber(Guid id);

    List<EventNotificationMethod> getEventNotificationMethodsById(int method_id);

    /**
     * Saves the provided subscriber.
     *
     * @param subscriber
     *            the subscriber
     */
    void subscribe(event_subscriber subscriber);

    /**
     * Updates the provided subscriber.
     *
     * @param subscriber
     *            the subscriber
     * @param oldMethodId
     *            TODO
     */
    void update(event_subscriber subscriber, int oldMethodId);

    /**
     * Removes the specified subscriber.
     *
     * @param subscriber
     *            the subscriber
     */
    void unsubscribe(event_subscriber subscriber);

    List<EventMap> getEventMapByName(String event_up_name);
}
