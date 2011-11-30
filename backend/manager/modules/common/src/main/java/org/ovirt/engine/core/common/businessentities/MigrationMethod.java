package org.ovirt.engine.core.common.businessentities;

public enum MigrationMethod {
    OFFLINE,
    ONLINE;

    public int getValue() {
        return this.ordinal();
    }

    public static MigrationMethod forValue(int value) {
        return values()[value];
    }
}
