package org.ovirt.engine.core.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.compat.Version;

/**
 * A utility function to match between {@link Version}s and {@link StorageFormatType}s
 */
public class VersionStorageFormatUtil {
    private static final Map<Version, StorageFormatType> versionToFormat = new TreeMap<>();
    static {
        versionToFormat.put(Version.v4_2, StorageFormatType.V4);
        versionToFormat.put(Version.v4_3, StorageFormatType.V5);
        versionToFormat.put(Version.v4_4, StorageFormatType.V5);
        versionToFormat.put(Version.v4_5, StorageFormatType.V5);
        versionToFormat.put(Version.v4_6, StorageFormatType.V5);
        versionToFormat.put(Version.v4_7, StorageFormatType.V5);
    };

    private static final Map<StorageFormatType, Version> earliestVersionSupported = new TreeMap<>();
    static {
        // Since versionToFormat is sorted in ascending order of versions, we'll always put
        // the earliest version at the end, overriding the lower ones
        // This is in fact cheaper than iterating the other way and checking if the key already
        // exists in the map
        List<Map.Entry<Version, StorageFormatType>> entries = new ArrayList<>(versionToFormat.entrySet());
        for (int i = entries.size() - 1; i >= 0; --i) {
            Map.Entry<Version, StorageFormatType> entry = entries.get(i);
            earliestVersionSupported .put(entry.getValue(), entry.getKey());
        }
    }

    public static StorageFormatType getForVersion(Version v) {
        return versionToFormat.get(v);
    }

    public static Version getEarliestVersionSupported (StorageFormatType type) {
        return earliestVersionSupported.get(type);
    }
}
