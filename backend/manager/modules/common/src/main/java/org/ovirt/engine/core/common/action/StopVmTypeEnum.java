package org.ovirt.engine.core.common.action;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "StopVmTypeEnum")
public enum StopVmTypeEnum {
    NORMAL,
    CANNOT_SHUTDOWN;

    public int getValue() {
        return this.ordinal();
    }

    public static StopVmTypeEnum forValue(int value) {
        return values()[value];
    }
}
