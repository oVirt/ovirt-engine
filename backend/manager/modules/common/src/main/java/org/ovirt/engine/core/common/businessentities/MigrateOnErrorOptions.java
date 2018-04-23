package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

public enum MigrateOnErrorOptions {
    NO(0),
    YES(1),
    HA_ONLY(2);

    private int intValue;

    private static final Map<Integer, MigrateOnErrorOptions> mappings = new HashMap<>();

    static {
        for (MigrateOnErrorOptions option : values()) {
            mappings.put(option.getValue(), option);
        }
    }

    private MigrateOnErrorOptions(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static MigrateOnErrorOptions forValue(int value) {
        return mappings.get(value);
    }
}
