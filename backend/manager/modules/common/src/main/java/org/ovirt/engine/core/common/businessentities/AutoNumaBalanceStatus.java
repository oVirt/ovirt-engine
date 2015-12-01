package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

public enum AutoNumaBalanceStatus {
    DISABLE(0),
    ENABLE(1),
    UNKNOWN(2);

    private int intValue;
    private static Map<Integer, AutoNumaBalanceStatus> mappings;

    static {
        mappings = new HashMap<>();
        for (AutoNumaBalanceStatus status : values()) {
            mappings.put(status.getValue(), status);
        }
    }

    private AutoNumaBalanceStatus(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static AutoNumaBalanceStatus forValue(int value) {
        return mappings.get(value);
    }
}
