package org.ovirt.engine.core.common.businessentities;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum StorageFormatType {

    V1("0"),
    V2("2"),
    V3("3"),
    V4("4"),
    V5("5");

    /**
     * This is a part of vdsm capabilities negotiation
     * system.
     *
     * Starting from 4.3 vdsm is reporting supported storage domain versions,
     * while engine uses that information to verify, whether it is safe
     * or not, to create storage domain of some specific version
     * in the current environment.
     *
     * Pre 4.3 vdsm do not report supported storage domain versions and
     * this functions provides a fixed default list of sd version for pre
     * 4.3 vdsms
     */
    private static final Set<StorageFormatType> defaultSupportedVersions =
            new HashSet<>(Arrays.asList(V1, V2, V3, V4));

    private final String value;
    private static final Map<String, StorageFormatType> mappings = new HashMap<>();
    static {
        for (StorageFormatType storageDomainFormat : values()) {
            mappings.put(storageDomainFormat.getValue(), storageDomainFormat);
        }
    }

    private StorageFormatType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static StorageFormatType getLatest() {
        return values()[values().length - 1];
    }

    public static StorageFormatType forValue(String value) {
        return mappings.get(value);
    }

    public static Set<StorageFormatType> getDefaultSupportedVersions() {
        return defaultSupportedVersions;
    }
}
