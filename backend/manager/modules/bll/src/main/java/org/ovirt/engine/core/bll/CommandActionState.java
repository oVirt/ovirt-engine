package org.ovirt.engine.core.bll;

public enum CommandActionState {
    EXECUTE, END_SUCCESS, END_FAILURE;

    public int getValue() {
        return this.ordinal();
    }

    public static CommandActionState forValue(int value) {
        return values()[value];
    }
}
