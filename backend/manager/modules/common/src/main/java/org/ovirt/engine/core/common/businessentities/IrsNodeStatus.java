package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "IrsNodeStatus")
public enum IrsNodeStatus {
    Offline(0),
    Online(1),
    Active(2),
    Standby(3);

    private int intValue;
    private static java.util.HashMap<Integer, IrsNodeStatus> mappings;

    private synchronized static java.util.HashMap<Integer, IrsNodeStatus> getMappings() {
        if (mappings == null) {
            mappings = new java.util.HashMap<Integer, IrsNodeStatus>();
        }
        return mappings;
    }

    private IrsNodeStatus(int value) {
        intValue = value;
        IrsNodeStatus.getMappings().put(value, this);
    }

    public int getValue() {
        return intValue;
    }

    public static IrsNodeStatus forValue(int value) {
        return getMappings().get(value);
    }
}
