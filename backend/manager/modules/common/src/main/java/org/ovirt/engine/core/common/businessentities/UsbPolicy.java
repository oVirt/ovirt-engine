package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "UsbPolicy")
public enum UsbPolicy {
    Enabled,
    Disabled;

    public int getValue() {
        return this.ordinal();
    }

    public static UsbPolicy forValue(int value) {
        return values()[value];
    }
}
