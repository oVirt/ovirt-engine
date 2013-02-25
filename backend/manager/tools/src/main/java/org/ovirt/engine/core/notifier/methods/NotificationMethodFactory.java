package org.ovirt.engine.core.notifier.methods;

import org.ovirt.engine.core.notifier.utils.sender.EventSender;

/**
 * An interface of notification method factories, enforcing the created class to derive from {@link EventSender}
 * @param <T>
 *            an implemented class of the {@link EventSender}
 */
public interface NotificationMethodFactory<T extends EventSender> {
    /**
     * Create an instance of the notification method implementation class
     * @return an implemented class instance of the {@link EventSender} which will be used to notify a subscription
     */
    public T createMethodClass();
}
