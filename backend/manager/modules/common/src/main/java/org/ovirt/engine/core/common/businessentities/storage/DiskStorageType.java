package org.ovirt.engine.core.common.businessentities.storage;

import java.util.HashMap;

import org.ovirt.engine.core.common.businessentities.Identifiable;

/**
 * Enum of the disk's type, which defines which underlying storage details will be contained in the {@link Disk}
 * object instance.
 */
public enum DiskStorageType implements Identifiable {
    IMAGE(0),
    LUN(1),
    CINDER(2);

    private int value;

    private static final HashMap<Integer, DiskStorageType> mappings = new HashMap<>();
    static {
        for (DiskStorageType storageType : values()) {
            mappings.put(storageType.getValue(), storageType);
        }
    }

    DiskStorageType(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    public static DiskStorageType forValue(int value) {
        return mappings.get(value);
    }

    public boolean isInternal() {
        return this == IMAGE || this == CINDER;
    }
}
