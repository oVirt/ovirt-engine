package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.compat.*;

public class event_subscriber_notification_methods implements Serializable {
    private static final long serialVersionUID = -5043610606679829895L;

    public event_subscriber_notification_methods() {
    }

    public event_subscriber_notification_methods(Guid subscriber_id, String event_up_name, String method_type) {
        this.subscriber_idField = subscriber_id;
        this.event_up_nameField = event_up_name;
        this.method_typeField = method_type;
    }

    private Guid subscriber_idField = new Guid();

    public Guid getsubscriber_id() {
        return this.subscriber_idField;
    }

    public void setsubscriber_id(Guid value) {
        this.subscriber_idField = value;
    }

    private String event_up_nameField;

    public String getevent_up_name() {
        return this.event_up_nameField;
    }

    public void setevent_up_name(String value) {
        this.event_up_nameField = value;
    }

    private String method_typeField;

    public String getmethod_type() {
        return this.method_typeField;
    }

    public void setmethod_type(String value) {
        this.method_typeField = value;
    }

}
