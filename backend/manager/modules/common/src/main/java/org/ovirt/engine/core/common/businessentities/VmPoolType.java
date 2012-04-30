package org.ovirt.engine.core.common.businessentities;

public enum VmPoolType {
    Automatic,
    Manual,
    TimeLease;

    public int getValue() {
        return this.ordinal();
    }

    public static VmPoolType forValue(int value) {
        return values()[value];
    }
}
