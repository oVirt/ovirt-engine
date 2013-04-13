package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

public enum DiskInterface {

    IDE(0),
    VirtIO(2);

    private int intValue;
    private static Map<Integer, DiskInterface> mappings;

    static {
        mappings = new HashMap<Integer, DiskInterface>();
        for (DiskInterface error : values()) {
            mappings.put(error.getValue(), error);
        }
    }

    private DiskInterface(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static DiskInterface forValue(int value) {
        return mappings.get(value);
    }
}
