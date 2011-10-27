package org.ovirt.engine.core.common;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "EventNotificationMethods")
public enum EventNotificationMethods {
    EMAIL(0);

    private int intValue;
    private static java.util.HashMap<Integer, EventNotificationMethods> mappings;

    private synchronized static java.util.HashMap<Integer, EventNotificationMethods> getMappings() {
        if (mappings == null) {
            mappings = new java.util.HashMap<Integer, EventNotificationMethods>();
        }
        return mappings;
    }

    private EventNotificationMethods(int value) {
        intValue = value;
        EventNotificationMethods.getMappings().put(value, this);
    }

    public int getValue() {
        return intValue;
    }

    public static EventNotificationMethods forValue(int value) {
        return getMappings().get(value);
    }

}
