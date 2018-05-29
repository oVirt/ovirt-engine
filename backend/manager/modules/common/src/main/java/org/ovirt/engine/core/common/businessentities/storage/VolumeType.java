package org.ovirt.engine.core.common.businessentities.storage;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Identifiable;

public enum VolumeType implements Identifiable {
    Unassigned(0),
    Preallocated(1),
    Sparse(2);

    private int intValue;
    private static final Map<Integer, VolumeType> mappings = new HashMap<>();

    static {
        for (VolumeType volumeType : values()) {
            mappings.put(volumeType.getValue(), volumeType);
        }
    }

    VolumeType(int value) {
        intValue = value;
    }

    @Override
    public int getValue() {
        return intValue;
    }

    public static VolumeType forValue(int value) {
        return mappings.get(value);
    }

}
