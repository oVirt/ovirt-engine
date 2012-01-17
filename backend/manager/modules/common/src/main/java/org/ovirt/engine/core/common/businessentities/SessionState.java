package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "SessionState")
public enum SessionState {
    Unknown,
    UserLoggedOn,
    Locked,
    Active,
    LoggedOff;

    public int getValue() {
        return this.ordinal();
    }

    public static SessionState forValue(int value) {
        return values()[value];
    }
}
