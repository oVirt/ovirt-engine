package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VmExitStatus")
public enum VmExitStatus {
    Normal,
    Error;

    public int getValue() {
        return this.ordinal();
    }

    public static VmExitStatus forValue(int value) {
        return values()[value];
    }
}
