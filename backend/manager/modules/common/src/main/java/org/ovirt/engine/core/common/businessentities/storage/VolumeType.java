package org.ovirt.engine.core.common.businessentities.storage;

import java.util.HashMap;

public enum VolumeType {
    Unassigned(0),
    Preallocated(1),
    Sparse(2);

    private int intValue;
    private static final HashMap<Integer, VolumeType> mappings = new HashMap<>();

    static {
        for (VolumeType volumeType : values()) {
            mappings.put(volumeType.getValue(), volumeType);
        }
    }

    private VolumeType(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static VolumeType forValue(int value) {
        return mappings.get(value);
    }

}
