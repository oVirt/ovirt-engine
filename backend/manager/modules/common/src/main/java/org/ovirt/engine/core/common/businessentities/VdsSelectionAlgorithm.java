package org.ovirt.engine.core.common.businessentities;

public enum VdsSelectionAlgorithm {
    None,
    EvenlyDistribute,
    PowerSave;

    public int getValue() {
        return this.ordinal();
    }

    public static VdsSelectionAlgorithm forValue(int value) {
        return values()[value];
    }
}
