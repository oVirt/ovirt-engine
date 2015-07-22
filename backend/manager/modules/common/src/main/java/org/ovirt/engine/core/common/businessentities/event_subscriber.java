package org.ovirt.engine.core.common.businessentities;

import org.hibernate.validator.constraints.Email;
import org.ovirt.engine.core.common.EventNotificationMethod;
import org.ovirt.engine.core.common.utils.ObjectUtils;
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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id.eventUpName == null) ? 0 : id.eventUpName.hashCode());
        result = prime * result + ((methodAddress == null) ? 0 : methodAddress.hashCode());
        result = prime * result + ((id.eventNotificationMethod == null) ? 0 : id.eventNotificationMethod.hashCode());
        result = prime * result + ((id.subscriberId == null) ? 0 : id.subscriberId.hashCode());
        result = prime * result + ((id.tagName == null) ? 0 : id.tagName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        event_subscriber other = (event_subscriber) obj;
        return (ObjectUtils.objectsEqual(id.eventUpName, other.id.eventUpName)
                && ObjectUtils.objectsEqual(methodAddress, other.methodAddress)
                && ObjectUtils.objectsEqual(id.eventNotificationMethod, other.id.eventNotificationMethod)
                && ObjectUtils.objectsEqual(id.subscriberId, other.id.subscriberId)
                && ObjectUtils.objectsEqual(id.tagName, other.id.tagName));
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
