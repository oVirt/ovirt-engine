package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "DiskType")
public enum DiskType {

    Unassigned,
    System,
    Data,
    Shared,
    Swap,
    Tempa;

    public int getValue() {
        return this.ordinal();
    }

    public static DiskType forValue(int value) {
        return values()[value];
    }
}
