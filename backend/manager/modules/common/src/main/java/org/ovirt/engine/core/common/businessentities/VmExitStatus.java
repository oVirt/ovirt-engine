package org.ovirt.engine.core.common.businessentities;

public enum VmExitStatus {
    Normal,
    Error;

    public int getValue() {
        return this.ordinal();
    }

    public static VmExitStatus forValue(int value) {
        return values()[value];
    }
}
