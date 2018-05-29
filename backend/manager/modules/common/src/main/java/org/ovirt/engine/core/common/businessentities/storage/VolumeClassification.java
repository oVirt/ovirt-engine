package org.ovirt.engine.core.common.businessentities.storage;

import java.util.HashMap;
import java.util.Map;

public enum VolumeClassification {
    Volume(0),
    Snapshot(1),
    Invalid(2);

    private int intValue;
    private static Map<Integer, VolumeClassification> mappings;

    static {
        mappings = new HashMap<>();
        for (VolumeClassification error : values()) {
            mappings.put(error.getValue(), error);
        }
    }

    VolumeClassification(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static VolumeClassification forValue(int value) {
        return mappings.get(value);
    }

    public static VolumeClassification getVolumeClassificationByActiveFlag(boolean isActive) {
        return isActive ? VolumeClassification.Volume : VolumeClassification.Snapshot;
    }
}
