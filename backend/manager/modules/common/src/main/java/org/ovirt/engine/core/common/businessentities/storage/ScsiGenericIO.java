package org.ovirt.engine.core.common.businessentities.storage;

import java.util.HashMap;
import java.util.Map;

public enum ScsiGenericIO {
    FILTERED(1),
    UNFILTERED(2);

    private int intValue;
    private static Map<Integer, ScsiGenericIO> mappings;

    static {
        mappings = new HashMap<>();
        for (ScsiGenericIO error : values()) {
            mappings.put(error.getValue(), error);
        }
    }

    ScsiGenericIO(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static ScsiGenericIO forValue(int value) {
        return mappings.get(value);
    }
}
