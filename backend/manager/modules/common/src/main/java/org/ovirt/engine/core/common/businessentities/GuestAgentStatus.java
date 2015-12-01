package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

public enum GuestAgentStatus {
    DoesntExist(0),
    Exists(1),
    UpdateNeeded(2);

    private static final Map<Integer, GuestAgentStatus> mappings = new HashMap<>();
    private int value;

    static {
        mappings.put(0, DoesntExist);
        mappings.put(1, Exists);
        mappings.put(2, UpdateNeeded);
    }

    GuestAgentStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static GuestAgentStatus forValue(int value) {
        return mappings.get(value);
    }

}
