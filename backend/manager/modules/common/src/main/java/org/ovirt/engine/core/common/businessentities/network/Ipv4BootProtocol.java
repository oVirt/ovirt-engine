package org.ovirt.engine.core.common.businessentities.network;

import java.util.HashMap;
import java.util.Map;

public enum Ipv4BootProtocol {
    NONE(0),
    DHCP(1),
    STATIC_IP(2, "static");

    private int intValue;
    private static Map<Integer, Ipv4BootProtocol> mappings;
    private String displayName;

    static {
        mappings = new HashMap<>();
        for (Ipv4BootProtocol value : values()) {
            mappings.put(value.getValue(), value);
        }
    }

    Ipv4BootProtocol(int intValue) {
        init(intValue, name().toLowerCase());
    }

    Ipv4BootProtocol(int intValue, String displayName) {
        init(intValue, displayName);
    }

    private void init(int intValue, String displayName) {
        this.intValue = intValue;
        this.displayName = displayName;
    }

    public int getValue() {
        return intValue;
    }

    public static Ipv4BootProtocol forValue(int value) {
        return mappings.get(value);
    }

    public String getDisplayName() {
        return displayName;
    }
}
