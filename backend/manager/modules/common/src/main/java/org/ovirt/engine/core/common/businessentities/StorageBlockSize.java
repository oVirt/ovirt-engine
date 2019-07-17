package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

public enum StorageBlockSize {
    BLOCK_AUTO(0),
    BLOCK_512(512),
    BLOCK_4K(4096);

    private final Integer value;
    private static final Map<Integer, StorageBlockSize> mappings = new HashMap<>();
    static {
        for (StorageBlockSize storageBlockSize : values()) {
            mappings.put(storageBlockSize.getValue(), storageBlockSize);
        }
    }

    private StorageBlockSize(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public static StorageBlockSize forValue(Integer value) {
        return mappings.get(value);
    }
}
