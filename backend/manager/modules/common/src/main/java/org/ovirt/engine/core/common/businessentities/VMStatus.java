package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VMStatus")
public enum VMStatus {
    Unassigned(-1),
    Down(0),
    Up(1),
    PoweringUp(2),
    PoweredDown(3),
    Paused(4),
    MigratingFrom(5),
    MigratingTo(6),
    Unknown(7),
    NotResponding(8),
    WaitForLaunch(9),
    RebootInProgress(10),
    SavingState(11),
    RestoringState(12),
    Suspended(13),
    ImageIllegal(14),
    ImageLocked(15),
    PoweringDown(16);

    private int intValue;
    private static java.util.HashMap<Integer, VMStatus> mappings = new HashMap<Integer, VMStatus>();

    static {
        for (VMStatus status : values()) {
            mappings.put(status.getValue(), status);
        }
    }

    private VMStatus(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static VMStatus forValue(int value) {
        return mappings.get(value);
    }

}
