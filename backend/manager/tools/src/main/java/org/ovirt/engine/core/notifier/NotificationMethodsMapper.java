package org.ovirt.engine.core.notifier;

import org.ovirt.engine.core.common.EventNotificationMethod;
import org.ovirt.engine.core.notifier.transport.Transport;
import org.ovirt.engine.core.notifier.transport.smtp.Smtp;
import org.ovirt.engine.core.notifier.utils.NotificationProperties;

import java.util.HashMap;
import java.util.Map;

public class NotificationMethodsMapper {

    private Map<EventNotificationMethod, Transport> eventSenders = new HashMap<>();

    public NotificationMethodsMapper(NotificationProperties prop) {
        eventSenders.put(EventNotificationMethod.EMAIL, new Smtp(prop));
    }

    public Transport getEventSender(EventNotificationMethod method) {
        return eventSenders.get(method);
    }

}
