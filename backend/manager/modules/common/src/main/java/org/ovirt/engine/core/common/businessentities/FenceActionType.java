package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "FenceActionType")
public enum FenceActionType {
    Restart(0),
    Start(1),
    Stop(2),
    Status(3);

    private int intValue;
    private static java.util.HashMap<Integer, FenceActionType> mappings;

    private synchronized static java.util.HashMap<Integer, FenceActionType> getMappings() {
        if (mappings == null) {
            mappings = new java.util.HashMap<Integer, FenceActionType>();
        }
        return mappings;
    }

    private FenceActionType(int value) {
        intValue = value;
        FenceActionType.getMappings().put(value, this);
    }

    public int getValue() {
        return intValue;
    }

    public static FenceActionType forValue(int value) {
        return getMappings().get(value);
    }
}
