package org.ovirt.engine.core.common.businessentities.network;

import java.util.HashMap;
import java.util.Map;

public enum Ipv6BootProtocol {
    NONE(0),
    DHCP(1),
    AUTOCONF(2),
    POLY_DHCP_AUTOCONF(3, "poly dhcp autoconf"),
    STATIC_IP(4, "static");

    private static Map<Integer, Ipv6BootProtocol> mappings;

    static {
        mappings = new HashMap<>();
        for (Ipv6BootProtocol value : values()) {
            mappings.put(value.getValue(), value);
        }
    }

    private int intValue;
    private String displayName;

    Ipv6BootProtocol(int intValue) {
        init(intValue, name().toLowerCase());
    }

    Ipv6BootProtocol(int intValue, String displayName) {
        init(intValue, displayName);
    }

    public static Ipv6BootProtocol forValue(int value) {
        return mappings.get(value);
    }

    private void init(int intValue, String displayName) {
        this.intValue = intValue;
        this.displayName = displayName;
    }

    public int getValue() {
        return intValue;
    }

    public String getDisplayName() {
        return displayName;
    }
}
