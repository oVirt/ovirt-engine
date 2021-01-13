package org.ovirt.engine.core.common.businessentities.storage;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Identifiable;

public enum DiskContentType implements Identifiable {

    DATA(0, "DATA"),
    OVF_STORE(1, "OVFS"),
    MEMORY_DUMP_VOLUME(2, "MEMD"),
    MEMORY_METADATA_VOLUME(3, "MEMM"),
    ISO(4, "ISOF"),
    HOSTED_ENGINE(5, "HEVD"),
    HOSTED_ENGINE_SANLOCK(6, "HESD"),
    HOSTED_ENGINE_METADATA(7, "HEMD"),
    HOSTED_ENGINE_CONFIGURATION(8, "HECI"),
    BACKUP_SCRATCH(9, "SCRD");

    public static final String LEGACY_DISK_TYPE = "2";

    private static final Map<Integer, DiskContentType> mappings = new HashMap<>();
    private static final Map<String, DiskContentType> storageMappings = new HashMap<>();
    private int value;
    private String storageValue;

    static {
        for (DiskContentType contentType : values()) {
            mappings.put(contentType.getValue(), contentType);
            storageMappings.put(contentType.getStorageValue(), contentType);
        }
        storageMappings.put(LEGACY_DISK_TYPE, DATA);
    }

    /**
     * Represents the actual content residing on the volume
     * @param value The value mapping stored in the database
     * @param storageValue The value stored in the storage, must be in the length of 4 characters (aside for the legacy
     *                     type which was always equal to "2"
     */
    DiskContentType(int value, String storageValue) {
        this.value = value;
        this.storageValue = storageValue;
    }

    @Override
    public int getValue() {
        return value;
    }

    public String getStorageValue() {
        return storageValue;
    }

    public static DiskContentType forValue(int value) {
        return mappings.get(value);
    }

    public static DiskContentType forStorageValue(String value) {
        return storageMappings.get(value);
    }

}
