package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

public enum StorageDomainType {

    Master(0),
    Data(1),
    ISO(2),
    ImportExport(3),
    Image(4),
    Volume(5),
    Unknown(6),
    ManagedBlockStorage(7),
    Unmanaged(8);

    private static final Map<Integer, StorageDomainType> mappings = new HashMap<>();

    static {
        for (StorageDomainType storageDomainType : values()) {
            mappings.put(storageDomainType.getValue(), storageDomainType);
        }
    }

    private int value;

    StorageDomainType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static StorageDomainType forValue(int value) {
        return mappings.get(value);
    }

    public boolean isDataDomain() {
        return this == Data || this == Master;
    }

    public boolean isIsoOrImportExportDomain() {
        return this == ISO || this == ImportExport;
    }

    public boolean isInternalDomain() {
        return isDataDomain() || isIsoOrImportExportDomain();
    }

    public boolean isKubevirtDomain() {
        return this == Unmanaged;
    }
}
