package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.common.EventNotificationMethods;
import org.ovirt.engine.core.common.utils.EnumUtils;

public class EventNotificationMethod implements Serializable {
    private static final long serialVersionUID = -8648391842192154307L;

    public EventNotificationMethod() {
    }

    public EventNotificationMethod(int method_id, String method_type) {
        this.methodId = method_id;
        this.methodType = method_type;
    }

    private int methodId;

    public int getmethod_id() {
        return this.methodId;
    }

    public void setmethod_id(int value) {
        this.methodId = value;
    }

    private String methodType;

    public EventNotificationMethods getmethod_type() {
        return EnumUtils.valueOf(EventNotificationMethods.class, methodType, true);
    }

    public void setmethod_type(EventNotificationMethods value) {
        this.methodType = value.name();
    }

}
