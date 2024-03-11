package org.ovirt.engine.core.common.businessentities.storage;

import java.util.HashMap;
import java.util.Map;

public enum Qcow2BitmapInfoFlags {
    IN_USE("in-use"),
    AUTO("auto");

    private String value;
    private static Map<String, Qcow2BitmapInfoFlags> mappings;

    static {
        mappings = new HashMap<>();
        for (Qcow2BitmapInfoFlags error : values()) {
            mappings.put(error.getValue(), error);
        }
    }

    Qcow2BitmapInfoFlags(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Qcow2BitmapInfoFlags forValue(String value) {
        return mappings.get(value);
    }
}
