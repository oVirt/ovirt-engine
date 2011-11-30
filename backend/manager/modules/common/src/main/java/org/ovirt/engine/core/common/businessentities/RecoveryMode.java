package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "RecoveryMode")
public enum RecoveryMode {
    Manual,
    Safe,
    Fast;

    public int getValue() {
        return this.ordinal();
    }

    public static RecoveryMode forValue(int value) {
        return values()[value];
    }
}
