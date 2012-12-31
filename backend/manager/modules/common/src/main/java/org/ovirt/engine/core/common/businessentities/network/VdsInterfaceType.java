package org.ovirt.engine.core.common.businessentities.network;

import java.util.HashMap;

public enum VdsInterfaceType {
    // we use this enum with bit manipulation
    None(0), // 00000
    General(1), // 00001
    Management(2), // 00010
    Storage(4), // 00100
    Guests(8); // 01000

    private int intValue;
    private static java.util.HashMap<Integer, VdsInterfaceType> mappings = new HashMap<Integer, VdsInterfaceType>();

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
