package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "FenceActionType")
public enum FenceActionType {
    Restart,
    Start,
    Stop,
    Status;

    public int getValue() {
        return this.ordinal();
    }

    public static FenceActionType forValue(int value) {
        return values()[value];
    }
}
