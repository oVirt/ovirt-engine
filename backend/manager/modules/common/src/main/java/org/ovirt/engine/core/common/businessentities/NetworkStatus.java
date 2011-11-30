package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "NetworkStatus")
public enum NetworkStatus {
    NonOperational,
    Operational;

    public int getValue() {
        return this.ordinal();
    }

    public static NetworkStatus forValue(int value) {
        return values()[value];
    }
}
