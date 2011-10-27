package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "VDSStatus")
public enum VDSStatus {
    Unassigned(0),
    Down(1),
    Maintenance(2),
    Up(3),
    NonResponsive(4),
    Error(5),
    Installing(6),
    InstallFailed(7),
    Reboot(
            8),
    PreparingForMaintenance(9),
    NonOperational(10),
    PendingApproval(11),
    Initializing(12),
    Problematic(13);

    private int intValue;
    private static java.util.HashMap<Integer, VDSStatus> mappings = new java.util.HashMap<Integer, VDSStatus>();

    static {
        for (VDSStatus status : values()) {
            mappings.put(status.getValue(), status);
        }
    }

    private VDSStatus(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static VDSStatus forValue(int value) {
        return mappings.get(value);
    }
}
