package org.ovirt.engine.core.common.businessentities.network;

import java.util.HashMap;
import java.util.Map;

public enum VdsInterfaceType {
    // we use this enum with bit manipulation
    NONE(0), // 00000
    GENERAL(1), // 00001
    MANAGEMENT(2), // 00010
    STORAGE(4), // 00100
    GUESTS(8); // 01000

    private int intValue;
    private static final Map<Integer, VdsInterfaceType> mappings = new HashMap<>();

    static {
        for (VdsInterfaceType vdsInterfaceType : values()) {
            mappings.put(vdsInterfaceType.getValue(), vdsInterfaceType);
        }
    }

    private VdsInterfaceType(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static VdsInterfaceType forValue(int value) {
        return mappings.get(value);
    }
}
