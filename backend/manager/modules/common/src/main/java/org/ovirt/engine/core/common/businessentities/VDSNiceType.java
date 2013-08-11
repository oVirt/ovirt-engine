package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

public enum VDSNiceType implements Identifiable {
    RHEL(0),
    RHEVH(2);

    private int intValue;
    private static final java.util.HashMap<Integer, VDSNiceType> mappings = new HashMap<Integer, VDSNiceType>();

    static {
        for (VDSNiceType type : values()) {
            mappings.put(type.getValue(), type);
        }
    }

    private VDSNiceType(int value) {
        intValue = value;
    }

    @Override
    public int getValue() {
        return intValue;
    }

    public static VDSNiceType forValue(int value) {
        return mappings.get(value);
    }
}
