package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

public enum VDSStatus implements Identifiable {
    Unassigned(0),
    Down(1),
    Maintenance(2),
    Up(3),
    NonResponsive(4),
    Error(5),
    Installing(6),
    InstallFailed(7),
    Reboot(8),
    PreparingForMaintenance(9),
    NonOperational(10),
    PendingApproval(11),
    Initializing(12),
    Connecting(13),
    InstallingOS(14),
    Kdumping(15);

    private static final HashMap<Integer, VDSStatus> mappings = new HashMap<>();
    private int intValue;

    static {
        for (VDSStatus status : values()) {
            mappings.put(status.getValue(), status);
        }
    }

    private VDSStatus(int value) {
        intValue = value;
    }

    @Override
    public int getValue() {
        return intValue;
    }

    public static VDSStatus forValue(int value) {
        return mappings.get(value);
    }
}
