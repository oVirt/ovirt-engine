package org.ovirt.engine.core.common.businessentities;

public enum InitializationType {
    None,
    Sysprep,
    CloudInit,
    Ignition;

    public int getValue() {
        return this.ordinal();
    }

    public static InitializationType forValue(int value) {
        return values()[value];
    }
}
