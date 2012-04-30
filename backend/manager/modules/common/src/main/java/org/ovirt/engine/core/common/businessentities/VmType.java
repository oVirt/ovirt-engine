package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

public enum VmType {
    Desktop(0),
    Server(1);

    private int intValue;
    private static java.util.HashMap<Integer, VmType> mappings = new HashMap<Integer, VmType>();

    static {
        for (VmType vmType : values()) {
            mappings.put(vmType.getValue(), vmType);
        }
    }

    private VmType(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static VmType forValue(int value) {
        return mappings.get(value);
    }
}
