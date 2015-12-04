package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.EventSubscriber;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>EventDao</code> defines a type for performing CRUD operations on instances of {@link EventSubscriber}.
 *
 *
 */
public interface EventDao extends Dao {
    /**
     * Retrieves all event subscriptions for the given subscriber id.
     *
     * @param id
     *            the subscriber id
     * @return the subscriptions
     */
    List<EventSubscriber> getAllForSubscriber(Guid id);

    /**
     * Saves the provided subscriber.
     *
     * @param subscriber
     *            the subscriber
     */
    void subscribe(EventSubscriber subscriber);

    /**
     * Removes the specified subscriber.
     *
     * @param subscriber
     *            the subscriber
     */
    void unsubscribe(EventSubscriber subscriber);

}
