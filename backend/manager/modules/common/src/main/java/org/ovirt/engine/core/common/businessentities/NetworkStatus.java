package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "NetworkStatus")
public enum NetworkStatus {
    NonOperational(0),
    Operational(1);

    private int intValue;
    private static java.util.HashMap<Integer, NetworkStatus> mappings;

    private synchronized static java.util.HashMap<Integer, NetworkStatus> getMappings() {
        if (mappings == null) {
            mappings = new java.util.HashMap<Integer, NetworkStatus>();
        }
        return mappings;
    }

    private NetworkStatus(int value) {
        intValue = value;
        NetworkStatus.getMappings().put(value, this);
    }

    public int getValue() {
        return intValue;
    }

    public static NetworkStatus forValue(int value) {
        return getMappings().get(value);
    }
}
