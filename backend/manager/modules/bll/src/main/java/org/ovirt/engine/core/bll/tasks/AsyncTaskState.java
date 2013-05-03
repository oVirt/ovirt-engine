package org.ovirt.engine.core.bll.tasks;

public enum AsyncTaskState {
    Initializing,
    Polling,
    Ended,
    AttemptingEndAction,
    ClearFailed,
    Cleared;

    public int getValue() {
        return this.ordinal();
    }

    public static AsyncTaskState forValue(int value) {
        return values()[value];
    }
}
