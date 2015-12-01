package org.ovirt.engine.core.common.businessentities.storage;

import java.util.HashMap;
import java.util.Map;

public enum DiskAlignment {

    Unknown(0),
    NotApplicable(1), // future use, e.g. disks with no partition table
    Misaligned(2),
    Aligned(3);

    private int value;
    private static final Map<Integer, DiskAlignment> mappings;

    static {
        mappings = new HashMap<>();

        for (DiskAlignment enumValue : values()) {
            mappings.put(enumValue.getValue(), enumValue);
        }
    }

    private DiskAlignment(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DiskAlignment forValue(int value) {
        return mappings.get(value);
    }
}
