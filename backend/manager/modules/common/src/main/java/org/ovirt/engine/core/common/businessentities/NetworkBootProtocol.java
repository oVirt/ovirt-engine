package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "NetworkBootProtocol")
public enum NetworkBootProtocol {
    None,
    Dhcp,
    StaticIp;

    public int getValue() {
        return this.ordinal();
    }

    public static NetworkBootProtocol forValue(int value) {
        return values()[value];
    }
}
