package org.ovirt.engine.core.common.businessentities;

public enum AsyncTaskResultEnum {
    success,
    failure,
    cleanSuccess,
    cleanFailure,
    unknown;

    public int getValue() {
        return this.ordinal();
    }

    public static AsyncTaskResultEnum forValue(int value) {
        return values()[value];
    }
}
