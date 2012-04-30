package org.ovirt.engine.core.common.businessentities;

public enum SpmStatus {
    SPM,
    Contend,
    Free,
    Unknown_Pool,
    SPM_ERROR;

    public int getValue() {
        return this.ordinal();
    }

    public static SpmStatus forValue(int value) {
        return values()[value];
    }

}
