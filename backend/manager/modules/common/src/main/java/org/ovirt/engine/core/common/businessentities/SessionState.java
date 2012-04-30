package org.ovirt.engine.core.common.businessentities;

public enum SessionState {
    Unknown,
    UserLoggedOn,
    Locked,
    Active,
    LoggedOff;

    public int getValue() {
        return this.ordinal();
    }

    public static SessionState forValue(int value) {
        return values()[value];
    }
}
