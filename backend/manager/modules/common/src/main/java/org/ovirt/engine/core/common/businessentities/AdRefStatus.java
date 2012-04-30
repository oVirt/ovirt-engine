package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

public enum AdRefStatus {
    Inactive(0),
    Active(1);

    private int intValue;
    private static java.util.HashMap<Integer, AdRefStatus> mappings = new HashMap<Integer, AdRefStatus>();

    static {
        for (AdRefStatus status : values()) {
            mappings.put(status.getValue(), status);
        }
    }

    private AdRefStatus(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static AdRefStatus forValue(int value) {
        return mappings.get(value);
    }
}
