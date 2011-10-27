package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "VmPoolType")
public enum VmPoolType {
    Automatic,
    Manual,
    TimeLease;

    public int getValue() {
        return this.ordinal();
    }

    public static VmPoolType forValue(int value) {
        return values()[value];
    }
}
