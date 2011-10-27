package org.ovirt.engine.core.common;

public enum EventNotificationEntity {
    UNKNOWN(0),
    Host(1),
    Vm(2),
    Storage(3),
    Engine(4);

    private int intValue;
    private static java.util.HashMap<Integer, EventNotificationEntity> mappings;

    private synchronized static java.util.HashMap<Integer, EventNotificationEntity> getMappings() {
        if (mappings == null) {
            mappings = new java.util.HashMap<Integer, EventNotificationEntity>();
        }
        return mappings;
    }

    private EventNotificationEntity(int value) {
        intValue = value;
        EventNotificationEntity.getMappings().put(value, this);
    }

    public int getValue() {
        return intValue;
    }

    public static EventNotificationEntity forValue(int value) {
        return getMappings().get(value);
    }

}
