package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "DisplayType")
public enum DisplayType {
    vnc,
    qxl;

    public int getValue() {
        return this.ordinal();
    }

    public static DisplayType forValue(int value) {
        return values()[value];
    }
}
