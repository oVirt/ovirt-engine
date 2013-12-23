package org.ovirt.engine.core.common.utils;

import java.util.HashMap;
import java.util.Map;

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
            if (type.isBlockDomain()) {
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
            new HashMap<Version, StorageFormatTypeMapper>() {
                {
                    put(Version.v2_2, new ConstantStorageFormatTypeMapper(StorageFormatType.V1));
                    put(Version.v3_0, new V2FormatTypeMapper());
                    put(Version.v3_1, new ConstantStorageFormatTypeMapper(StorageFormatType.V3));
                    put(Version.v3_2, new ConstantStorageFormatTypeMapper(StorageFormatType.V3));
                    put(Version.v3_3, new ConstantStorageFormatTypeMapper(StorageFormatType.V3));
                    put(Version.v3_4, new ConstantStorageFormatTypeMapper(StorageFormatType.V3));
                }
            };

    public static StorageFormatType getPreferredForVersion(Version v, StorageType type) {
        return versionToFormat.get(v).getPreferred(type);
    }

    public static StorageFormatType getRequiredForVersion(Version v, StorageType type) {
        return versionToFormat.get(v).getRequired(type);
    }
}
