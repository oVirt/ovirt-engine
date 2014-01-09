package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.common.EventNotificationMethod;
import org.ovirt.engine.core.compat.Guid;

import java.io.Serializable;

public class AuditLogEventSubscriber implements Serializable, EventFilter {

    private EventNotificationMethod eventNotificationMethod;

    private String methodAddress;

    private Guid subscriberId;

    private String username;

    private EventFilter eventFilter;

    public EventNotificationMethod getEventNotificationMethod() {
        return eventNotificationMethod;
    }

    public void setEventNotificationMethod(EventNotificationMethod eventNotificationMethod) {
        this.eventNotificationMethod = eventNotificationMethod;
    }

    public String getMethodAddress() {
        return methodAddress;
    }

    public void setMethodAddress(String methodAddress) {
        this.methodAddress = methodAddress;
    }

    public Guid getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(Guid subscriberId) {
        this.subscriberId = subscriberId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public EventFilter getEventFilter() {
        return eventFilter;
    }

    public void setEventFilter(EventFilter eventFilter) {
        this.eventFilter = eventFilter;
    }

    @Override
    public boolean isSubscribed(AuditLogEvent event) {
        return eventFilter.isSubscribed(event);
    }
}
