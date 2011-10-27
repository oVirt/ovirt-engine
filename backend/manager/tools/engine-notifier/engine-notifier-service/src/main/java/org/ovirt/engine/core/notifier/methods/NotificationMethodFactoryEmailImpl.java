package org.ovirt.engine.core.notifier.methods;

import java.util.Map;

import org.ovirt.engine.core.notifier.utils.sender.mail.EventSenderMailImpl;

/**
 * A factory of the email notification method class.<br>
 * A single {@code EventSenderMailImpl} instance could serve multiple notification actions, therefore the factory will
 * instantiate a single instance of it and will provide it to the dispatchers.
 * @see NotificationMethodFactory
 */
public class NotificationMethodFactoryEmailImpl implements NotificationMethodFactory<EventSenderMailImpl> {

    private EventSenderMailImpl senderMailImpl = null;

    public NotificationMethodFactoryEmailImpl(Map<String,String> properties) {
        senderMailImpl = new EventSenderMailImpl(properties);
    }

    /**
     * Returns a created email notification method
     */
    @Override
    public EventSenderMailImpl createMethodClass() {
        return senderMailImpl;
    }
}
