package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import org.hibernate.validator.constraints.Email;
import org.ovirt.engine.core.common.EventNotificationMethod;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringFormat;

public class EventSubscriber implements Queryable {
    private static final long serialVersionUID = 5899827011779820180L;

    private EventSubscriberId id;

    public EventSubscriber() {
        id = new EventSubscriberId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                methodAddress
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EventSubscriber)) {
            return false;
        }
        EventSubscriber other = (EventSubscriber) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(methodAddress, other.methodAddress);
    }

    public EventSubscriber(String eventUpName, EventNotificationMethod eventNotificationMethod,
                           Guid subscriberId, String tagName) {
        this();
        this.id.eventUpName = eventUpName;
        this.id.eventNotificationMethod = eventNotificationMethod;
        this.methodAddress = "";
        this.id.subscriberId = subscriberId;
        this.id.tagName = tagName;
    }

    public EventSubscriber(String eventUpName, EventNotificationMethod eventNotificationMethod, String methodAddress, Guid subscriberId,
                           String tagName) {
        this (eventUpName, eventNotificationMethod, subscriberId, tagName);
        this.methodAddress = methodAddress;
    }

    public String getEventUpName() {
        return this.id.eventUpName;
    }

    public void setEventUpName(String value) {
        this.id.eventUpName = value;
    }

    public EventNotificationMethod getEventNotificationMethod() {
        return this.id.eventNotificationMethod;
    }

    public void setEventNotificationMethod(EventNotificationMethod eventNotificationMethod) {
        this.id.eventNotificationMethod = eventNotificationMethod;
    }

    @Email(message = "VALIDATION_EVENTS_EMAIL_FORMAT")
    private String methodAddress;

    public String getMethodAddress() {
        return this.methodAddress;
    }

    public void setMethodAddress(String value) {
        this.methodAddress = value;
    }

    public Guid getSubscriberId() {
        return this.id.subscriberId;
    }

    public void setSubscriberId(Guid value) {
        this.id.subscriberId = value;
    }

    public String getTagName() {
        return this.id.tagName;
    }

    public void setTagName(String value) {
        this.id.tagName = value;
    }

    // if there will be subscribers edit we should add unique field to this
    // table
    @Override
    public Object getQueryableId() {
        return StringFormat.format("%1$s%2$s%3$s%4$s", id.eventUpName, id.eventNotificationMethod, id.subscriberId,
                id.tagName == null ? "" : id.tagName);
    }
}
