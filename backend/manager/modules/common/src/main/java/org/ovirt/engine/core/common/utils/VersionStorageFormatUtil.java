package org.ovirt.engine.core.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.compat.Version;

/**
 * A utility function to match between {@link Version}s and {@link StorageFormatType}s
 */
public class VersionStorageFormatUtil {
    private interface StorageFormatTypeMapper {
        StorageFormatType getPreferred(StorageType t);
        StorageFormatType getRequired(StorageType t);
    }

    private static class ConstantStorageFormatTypeMapper implements StorageFormatTypeMapper {
        private StorageFormatType formatType;

        ConstantStorageFormatTypeMapper(StorageFormatType formatType) {
            this.formatType = formatType;
        }

        @Override
        public StorageFormatType getPreferred(StorageType t) {
            return formatType;
        }

        @Override
        public StorageFormatType getRequired(StorageType t) {
            return formatType;
        }
    }

    private static class V2FormatTypeMapper implements StorageFormatTypeMapper {
        @Override
        public StorageFormatType getPreferred(StorageType type) {
            if (type != null && type.isBlockDomain()) {
                return StorageFormatType.V2;
            }
            return StorageFormatType.V1;
        }

        @Override
        public StorageFormatType getRequired(StorageType t) {
            return StorageFormatType.V1;
        }
    }

    private static final Map<Version, StorageFormatTypeMapper> versionToFormat =
            new TreeMap<Version, StorageFormatTypeMapper>() {
                {
                    put(Version.v3_0, new V2FormatTypeMapper());
                    put(Version.v3_1, new ConstantStorageFormatTypeMapper(StorageFormatType.V3));
                    put(Version.v3_2, new ConstantStorageFormatTypeMapper(StorageFormatType.V3));
                    put(Version.v3_3, new ConstantStorageFormatTypeMapper(StorageFormatType.V3));
                    put(Version.v3_4, new ConstantStorageFormatTypeMapper(StorageFormatType.V3));
                    put(Version.v3_5, new ConstantStorageFormatTypeMapper(StorageFormatType.V3));
                    put(Version.v3_6, new ConstantStorageFormatTypeMapper(StorageFormatType.V3));
                }
            };

    private static final Map<StorageFormatType, Version> earliestVersionSupported =
            new TreeMap<StorageFormatType, Version>() {
                {
                    // Since versionToFormat is sorted in ascending order of versions, we'll always put
                    // the earliest version at the end, overriding the lower ones
                    // This is in fact cheaper than iterating the other way and checking if the key already
                    // exists in the map
                    List<Map.Entry<Version, StorageFormatTypeMapper>> entries =
                            new ArrayList<Map.Entry<Version, StorageFormatTypeMapper>>(versionToFormat.entrySet());
                    for (int i = entries.size() - 1; i >= 0; --i) {
                        Map.Entry<Version, StorageFormatTypeMapper> entry = entries.get(i);
                        // iSCSI is always the strictest storage type.
                        // If this assumption is broken, the flow should be revisited
                        put(entry.getValue().getRequired(StorageType.ISCSI), entry.getKey());
                    }
                }
            };

    public static StorageFormatType getPreferredForVersion(Version v, StorageType type) {
        return versionToFormat.get(v).getPreferred(type);
    }

    public static StorageFormatType getRequiredForVersion(Version v, StorageType type) {
        return versionToFormat.get(v).getRequired(type);
    }

    public static Version getEarliestVersionSupported (StorageFormatType type) {
        return earliestVersionSupported.get(type);
    }
}
