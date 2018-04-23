package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

public enum VDSType {
    VDS(0),
    oVirtNode(1),
    oVirtVintageNode(2);

    private int intValue;
    private static final Map<Integer, VDSType> mappings = new HashMap<>();

    static {
        for (VDSType type : values()) {
            mappings.put(type.getValue(), type);
        }
    }

    private VDSType(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static VDSType forValue(int value) {
        return mappings.get(value);
    }
}
