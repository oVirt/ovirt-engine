package org.ovirt.engine.core.common.businessentities.storage;

public enum LunStatus {
    Free,
    Used,
    Unusable;

    public int getValue() {
        return this.ordinal();
    }

    public static LunStatus forValue(int value) {
        return values()[value];
    }
}
