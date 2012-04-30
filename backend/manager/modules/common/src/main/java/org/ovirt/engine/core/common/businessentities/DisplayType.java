package org.ovirt.engine.core.common.businessentities;

public enum DisplayType {
    vnc,
    qxl;

    public int getValue() {
        return this.ordinal();
    }

    public static DisplayType forValue(int value) {
        return values()[value];
    }
}
