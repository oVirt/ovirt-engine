package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

public enum DiskAlignment {

    Unknown(0),
    NotApplicable(1), // future use, e.g. disks with no partition table
    Misaligned(2),
    Aligned(3);

    private int intValue;
    private static final Map<Integer, DiskAlignment> mappings;

    static {
        mappings = new HashMap<Integer, DiskAlignment>();

        for (DiskAlignment enumValue : values()) {
            mappings.put(enumValue.getValue(), enumValue);
        }
    }

    private DiskAlignment(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static DiskAlignment forValue(int value) {
        return mappings.get(value);
    }
}
