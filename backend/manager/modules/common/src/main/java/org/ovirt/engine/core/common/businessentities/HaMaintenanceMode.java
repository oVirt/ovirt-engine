package org.ovirt.engine.core.common.businessentities;

public enum HaMaintenanceMode {
    GLOBAL,
    LOCAL;

    public int getValue() {
        return this.ordinal();
    }

    public static HaMaintenanceMode forValue(int value) {
        return values()[value];
    }
}
