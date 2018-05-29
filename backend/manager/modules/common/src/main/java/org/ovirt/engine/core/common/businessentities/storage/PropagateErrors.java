package org.ovirt.engine.core.common.businessentities.storage;

import java.util.HashMap;
import java.util.Map;

public enum PropagateErrors {
    Off(0),
    On(1);

    private int intValue;
    private static Map<Integer, PropagateErrors> mappings;

    static {
        mappings = new HashMap<>();
        for (PropagateErrors error : values()) {
            mappings.put(error.getValue(), error);
        }
    }

    PropagateErrors(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static PropagateErrors forValue(int value) {
        return mappings.get(value);
    }
}
