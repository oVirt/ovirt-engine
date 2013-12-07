package org.ovirt.engine.core.notifier.utils;

import org.ovirt.engine.core.common.EventNotificationMethods;
import org.ovirt.engine.core.notifier.utils.sender.EventSender;
import org.ovirt.engine.core.notifier.utils.sender.mail.EventSenderMailImpl;

import java.util.HashMap;
import java.util.Map;

public class NotificationMethodsMapper {

    private Map<EventNotificationMethods, EventSender> eventSenders = new HashMap<>();

    public NotificationMethodsMapper(NotificationProperties prop) {
        eventSenders.put(EventNotificationMethods.EMAIL, new EventSenderMailImpl(prop));
    }

    public EventSender getEventSender(EventNotificationMethods method) {
        return eventSenders.get(method);
    }

}
