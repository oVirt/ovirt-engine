package org.ovirt.engine.core.common.utils;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.compat.Version;

/**
 * A utility function to match between {@link Version}s and {@link StorageFormatType}s
 */
public class VersionStorageFormatUtil {
    private static final Map<Version, StorageFormatType> versionToFormat = new HashMap<Version, StorageFormatType>() {
        {
            put(Version.v2_2, StorageFormatType.V1);
            put(Version.v3_0, StorageFormatType.V2); // Only relevant for block domains
            put(Version.v3_1, StorageFormatType.V3);
            put(Version.v3_2, StorageFormatType.V3);
            put(Version.v3_3, StorageFormatType.V3);
        }
    };

    public static StorageFormatType getFormatForVersion(Version v) {
        return versionToFormat.get(v);
    }
}
