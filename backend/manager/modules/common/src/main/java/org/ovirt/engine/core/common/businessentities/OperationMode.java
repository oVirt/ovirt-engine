package org.ovirt.engine.core.common.businessentities;

public enum OperationMode {
    FullVirtualized,
    ParaVirtualized;

    public int getValue() {
        return this.ordinal();
    }

    public static OperationMode forValue(int value) {
        return values()[value];
    }
}
