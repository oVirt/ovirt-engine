package org.ovirt.engine.core.common;

public enum EventNotificationEntity {
    UNKNOWN,
    Host,
    Vm,
    Storage,
    Engine,
    GlusterVolume;

    public int getValue() {
        return this.ordinal();
    }

    public static EventNotificationEntity forValue(int value) {
        return values()[value];
    }
}
