package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "HypervisorType")
public enum HypervisorType {
    KVM,
    Xen;

    public int getValue() {
        return this.ordinal();
    }

    public static HypervisorType forValue(int value) {
        return values()[value];
    }
}
