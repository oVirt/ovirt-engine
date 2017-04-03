package org.ovirt.engine.core.common.businessentities.storage;

import java.util.HashMap;

import org.ovirt.engine.core.common.businessentities.Identifiable;

public enum DiskContentType implements Identifiable {
    DATA(0),
    OVF_STORE(1),
    MEMORY_DUMP_VOLUME(2),
    MEMORY_METADATA_VOLUME(3);

    private static final HashMap<Integer, DiskContentType> mappings = new HashMap<>();
    private int value;

    static {
        for (DiskContentType contentType : values()) {
            mappings.put(contentType.getValue(), contentType);
        }
    }

    DiskContentType(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    public static DiskContentType forValue(int value) {
        return mappings.get(value);
    }
}
