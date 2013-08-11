package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

public enum VmType implements Identifiable {
    Desktop(0),
    Server(1);

    private int intValue;
    private static final java.util.HashMap<Integer, VmType> mappings = new HashMap<Integer, VmType>();

    static {
        for (VmType vmType : values()) {
            mappings.put(vmType.getValue(), vmType);
        }
    }

    private VmType(int value) {
        intValue = value;
    }

    @Override
    public int getValue() {
        return intValue;
    }

    public static VmType forValue(int value) {
        return mappings.get(value);
    }
}
