package org.ovirt.engine.core.common.businessentities.storage;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Identifiable;

/**
 * Enum of the disk's type, which defines which underlying storage details will be contained in the {@link Disk}
 * object instance.
 */
public enum DiskStorageType implements Identifiable {
    IMAGE(0),
    LUN(1),
    CINDER(2),
    MANAGED_BLOCK_STORAGE(3),
    KUBERNETES(4);

    private int value;

    private static final Map<Integer, DiskStorageType> mappings = new HashMap<>();
    static {
        for (DiskStorageType storageType : values()) {
            mappings.put(storageType.getValue(), storageType);
        }
    }

    DiskStorageType(int value) {
        this.value = value;
    }

    private static Map<Class<? extends Disk>, DiskStorageType> classToType = new HashMap<>();
    static {
        classToType.put(LunDisk.class, LUN);
        classToType.put(DiskImage.class, IMAGE);
        classToType.put(CinderDisk.class, CINDER);
        classToType.put(ManagedBlockStorageDisk.class, MANAGED_BLOCK_STORAGE);
    }

    public static DiskStorageType forClass(Class<? extends Disk> clazz) {
        return classToType.get(clazz);
    }

    @Override
    public int getValue() {
        return value;
    }

    public static DiskStorageType forValue(int value) {
        return mappings.get(value);
    }

    public boolean isInternal() {
        return this == IMAGE || this == CINDER || this == MANAGED_BLOCK_STORAGE;
    }
}
