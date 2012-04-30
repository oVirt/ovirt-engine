package org.ovirt.engine.core.common.businessentities;

public enum RecoveryMode {
    Manual,
    Safe,
    Fast;

    public int getValue() {
        return this.ordinal();
    }

    public static RecoveryMode forValue(int value) {
        return values()[value];
    }
}
