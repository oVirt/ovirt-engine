package org.ovirt.engine.core.common;

import java.util.HashMap;
import java.util.Map;

public enum EventNotificationMethods {
    EMAIL(0);

    private int intValue;
    private static Map<Integer, EventNotificationMethods> mappings;

    static {
        mappings = new HashMap<Integer, EventNotificationMethods>();
        for (EventNotificationMethods value : values()) {
            mappings.put(value.getValue(), value);
        }
    }

    private EventNotificationMethods(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static EventNotificationMethods forValue(int value) {
        return mappings.get(value);
    }

}
