package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

public enum VmExitReason {
    Unknown(-1),
    Success(0),
    GenericError(1),
    LostQEMUConnection(2),
    LibvirtStartFailed(3),
    MigrationSucceeded(4),
    SaveStateSucceeded(5),
    AdminShutdown(6),
    UserShutdown(7),
    MigrationFailed(8);

    private final int reason;
    private static final HashMap<Integer, VmExitReason> valueToReason = new HashMap<>();

    static {
        for (VmExitReason reason : values()) {
            valueToReason.put(reason.getValue(), reason);
        }
    }

    private VmExitReason(int value) {
        this.reason = value;
    }

    public int getValue() {
        return reason;
    }

    public static VmExitReason forValue(int value) {
        return valueToReason.get(value);
    }
}
