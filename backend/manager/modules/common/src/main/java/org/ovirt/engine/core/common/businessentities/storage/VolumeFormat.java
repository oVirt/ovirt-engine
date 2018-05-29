package org.ovirt.engine.core.common.businessentities.storage;

import java.util.HashMap;
import java.util.Map;

public enum VolumeFormat {
    // Added in order to keep the ordinal and array element values consistent
    UNUSED0(0), UNUSED1(1), UNUSED2(2),
    Unassigned(3),
    COW(4),
    RAW(5);

    private int intValue;
    private static final Map<Integer, VolumeFormat> mappings = new HashMap<>();

    static {
        for (VolumeFormat volumeFormat : values()) {
            mappings.put(volumeFormat.getValue(), volumeFormat);
        }
    }

    VolumeFormat(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static VolumeFormat forValue(int value) {
        return mappings.get(value);
    }

}
