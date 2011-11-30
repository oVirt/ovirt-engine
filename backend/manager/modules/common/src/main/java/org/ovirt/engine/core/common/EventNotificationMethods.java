package org.ovirt.engine.core.common;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "EventNotificationMethods")
public enum EventNotificationMethods {
    EMAIL;

    public int getValue() {
        return this.ordinal();
    }

    public static EventNotificationMethods forValue(int value) {
        return values()[value];
    }

}
