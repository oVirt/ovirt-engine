package org.ovirt.engine.core.common.businessentities.network;

import java.util.HashMap;
import java.util.Map;

public enum NetworkBootProtocol {
    NONE(0),
    DHCP(1),
    STATIC_IP(2, "static");

    private int intValue;
    private static Map<Integer, NetworkBootProtocol> mappings;
    private String displayName;

    static {
        mappings = new HashMap<>();
        for (NetworkBootProtocol error : values()) {
            mappings.put(error.getValue(), error);
        }
    }

    private NetworkBootProtocol(int value) {
        intValue = value;
        displayName = name().toLowerCase();
    }

    private NetworkBootProtocol(int value, String displayName) {
        intValue = value;
        this.displayName = displayName;
    }

    public int getValue() {
        return intValue;
    }

    public static NetworkBootProtocol forValue(int value) {
        return mappings.get(value);
    }

    public String getDisplayName() {
        return displayName;
    }
}
