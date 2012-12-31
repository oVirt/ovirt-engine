package org.ovirt.engine.core.common.businessentities.network;

import java.util.HashMap;
import java.util.Map;

public enum NetworkBootProtocol {
    None(0),
    Dhcp(1),
    StaticIp(2);

    private int intValue;
    private static Map<Integer, NetworkBootProtocol> mappings;

    static {
        mappings = new HashMap<Integer, NetworkBootProtocol>();
        for (NetworkBootProtocol error : values()) {
            mappings.put(error.getValue(), error);
        }
    }

    private NetworkBootProtocol(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static NetworkBootProtocol forValue(int value) {
        return mappings.get(value);
    }
}
