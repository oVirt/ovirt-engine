package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import org.hibernate.validator.constraints.Email;
import org.ovirt.engine.core.common.EventNotificationMethod;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringFormat;

public class event_subscriber implements IVdcQueryable {
    private static final long serialVersionUID = 5899827011779820180L;

    private event_subscriber_id id;

    public event_subscriber() {
        id = new event_subscriber_id();
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
        if (!(obj instanceof event_subscriber)) {
            return false;
        }
        event_subscriber other = (event_subscriber) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(methodAddress, other.methodAddress);
    }

    public event_subscriber(String event_up_name, EventNotificationMethod eventNotificationMethod,
                            Guid subscriber_id, String tagName) {
        this();
        this.id.eventUpName = event_up_name;
        this.id.eventNotificationMethod = eventNotificationMethod;
        this.methodAddress = "";
        this.id.subscriberId = subscriber_id;
        this.id.tagName = tagName;
    }

    public event_subscriber(String event_up_name, EventNotificationMethod eventNotificationMethod, String method_address, Guid subscriber_id,
            String tagName) {
        this (event_up_name, eventNotificationMethod, subscriber_id, tagName);
        this.methodAddress = method_address;
    }

    public String getevent_up_name() {
        return this.id.eventUpName;
    }

    public void setevent_up_name(String value) {
        this.id.eventUpName = value;
    }

    public EventNotificationMethod getevent_notification_method() {
        return this.id.eventNotificationMethod;
    }

    public void setevent_notification_method(EventNotificationMethod eventNotificationMethod) {
        this.id.eventNotificationMethod = eventNotificationMethod;
    }

    @Email(message = "VALIDATION_EVENTS_EMAIL_FORMAT")
    private String methodAddress;

    public String getmethod_address() {
        return this.methodAddress;
    }

    public void setmethod_address(String value) {
        this.methodAddress = value;
    }

    public Guid getsubscriber_id() {
        return this.id.subscriberId;
    }

    public void setsubscriber_id(Guid value) {
        this.id.subscriberId = value;
    }

    public String gettag_name() {
        return this.id.tagName;
    }

    public void settag_name(String value) {
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
