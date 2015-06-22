package org.ovirt.engine.core.common.businessentities.storage;

import org.ovirt.engine.core.common.businessentities.Identifiable;

/**
 * Enum of the disk's type, which defines which underlying storage details will be contained in the {@link Disk}
 * object instance.
 */
public enum DiskStorageType implements Identifiable {
    // FIXME add ids and remove the ordinal impl of getValue
    IMAGE,
    LUN,
    CINDER;

    @Override
    public int getValue() {
        return this.ordinal();
    }

    public static DiskStorageType forValue(int value) {
        return values()[value];
    }

    public boolean isInternal() {
        return this == IMAGE || this == CINDER;
    }
}
