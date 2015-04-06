package org.ovirt.engine.core.common.businessentities.storage;

import org.ovirt.engine.core.common.businessentities.Nameable;

import java.util.HashMap;
import java.util.Map;

public enum CinderVolumeDriver implements Nameable {
    RBD("rbd");

    private String name;
    private static Map<String, CinderVolumeDriver> mappings;

    static {
        mappings = new HashMap<>();
        for (CinderVolumeDriver type : values()) {
            mappings.put(type.getName(), type);
        }
    }

    CinderVolumeDriver(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
