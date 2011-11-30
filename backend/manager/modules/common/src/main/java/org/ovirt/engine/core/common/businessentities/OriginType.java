package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "OriginType")
public enum OriginType {
    ENGINE,
    VMWARE,
    XEN;

    public int getValue() {
        return this.ordinal();
    }

    public static OriginType forValue(int value) {
        return values()[value];
    }
}
