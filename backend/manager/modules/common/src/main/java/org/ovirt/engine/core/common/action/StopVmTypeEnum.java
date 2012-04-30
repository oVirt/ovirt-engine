package org.ovirt.engine.core.common.action;

public enum StopVmTypeEnum {
    NORMAL,
    CANNOT_SHUTDOWN;

    public int getValue() {
        return this.ordinal();
    }

    public static StopVmTypeEnum forValue(int value) {
        return values()[value];
    }
}
