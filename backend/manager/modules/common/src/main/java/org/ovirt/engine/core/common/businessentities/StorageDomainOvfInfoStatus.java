package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

public enum StorageDomainOvfInfoStatus implements Identifiable{
    UPDATED(0), OUTDATED(1), DISABLED(2);

    private int value;
    private static final Map<Integer, StorageDomainOvfInfoStatus> map = new HashMap<>();

    static {
        for (StorageDomainOvfInfoStatus status : values()) {
            map.put(status.getValue(), status);
        }
    }

    private StorageDomainOvfInfoStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static StorageDomainOvfInfoStatus forValue(int value) {
        return map.get(value);
    }
}
