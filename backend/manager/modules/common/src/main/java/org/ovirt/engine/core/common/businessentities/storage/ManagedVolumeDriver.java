package org.ovirt.engine.core.common.businessentities.storage;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Nameable;

public enum ManagedVolumeDriver implements Nameable {
    RBD("rbd"),
    BLOCK("block");

    private String name;
    private static Map<String, ManagedVolumeDriver> mappings;

    static {
        mappings = new HashMap<>();
        for (ManagedVolumeDriver type : values()) {
            mappings.put(type.getName(), type);
        }
    }

    ManagedVolumeDriver(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static ManagedVolumeDriver forValue(String value) {
        return mappings.get(value);
    }
}
