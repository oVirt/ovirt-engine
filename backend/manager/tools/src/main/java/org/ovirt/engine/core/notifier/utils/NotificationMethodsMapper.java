package org.ovirt.engine.core.notifier.utils;

import org.ovirt.engine.core.common.EventNotificationMethod;
import org.ovirt.engine.core.notifier.utils.sender.EventSender;
import org.ovirt.engine.core.notifier.utils.sender.mail.EventSenderMailImpl;

import java.util.HashMap;
import java.util.Map;

public class NotificationMethodsMapper {

    private Map<EventNotificationMethod, EventSender> eventSenders = new HashMap<>();

    public NotificationMethodsMapper(NotificationProperties prop) {
        eventSenders.put(EventNotificationMethod.EMAIL, new EventSenderMailImpl(prop));
    }

    public EventSender getEventSender(EventNotificationMethod method) {
        return eventSenders.get(method);
    }

}
