package org.ovirt.engine.core.common.businessentities;

public enum FenceActionType {
    Restart,
    Start,
    Stop,
    Status;

    public int getValue() {
        return this.ordinal();
    }

    public static FenceActionType forValue(int value) {
        return values()[value];
    }
}
