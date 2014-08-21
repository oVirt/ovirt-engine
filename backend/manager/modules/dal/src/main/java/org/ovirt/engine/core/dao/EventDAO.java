package org.ovirt.engine.core.dao;

import java.util.List;

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

    /**
     * Saves the provided subscriber.
     *
     * @param subscriber
     *            the subscriber
     */
    void subscribe(event_subscriber subscriber);

    /**
     * Removes the specified subscriber.
     *
     * @param subscriber
     *            the subscriber
     */
    void unsubscribe(event_subscriber subscriber);

}
