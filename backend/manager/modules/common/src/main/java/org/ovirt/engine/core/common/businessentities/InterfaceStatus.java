package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "InterfaceStatus")
public enum InterfaceStatus {
    None,
    Up,
    Down;

    public int getValue() {
        return this.ordinal();
    }

    public static InterfaceStatus forValue(int value) {
        return values()[value];
    }
}
