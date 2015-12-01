package org.ovirt.engine.core.common.businessentities.network;

import java.util.HashMap;
import java.util.Map;

public enum NetworkStatus {
    NON_OPERATIONAL(0),
    OPERATIONAL(1);

    private int intValue;
    private static Map<Integer, NetworkStatus> mappings;

    static {
        mappings = new HashMap<>();
        for (NetworkStatus error : values()) {
            mappings.put(error.getValue(), error);
        }
    }

    private NetworkStatus(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static NetworkStatus forValue(int value) {
        return mappings.get(value);
    }
}
