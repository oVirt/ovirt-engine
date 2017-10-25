package org.ovirt.engine.core.common.businessentities;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum StorageDomainStatus implements Identifiable{
    Unknown(0),
    Uninitialized(1),
    Unattached(2),
    Active(3),
    Inactive(4),
    Locked(5),
    Maintenance(6),
    PreparingForMaintenance(7),
    Detaching(8),
    Activating(9);

    private int value;
    private static final Map<Integer, StorageDomainStatus> valueToStatus =
            Stream.of(values()).collect(Collectors.toMap(StorageDomainStatus::getValue, Function.identity()));

    StorageDomainStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static StorageDomainStatus forValue(int value) {
        return valueToStatus.get(value);
    }

    public boolean isStorageDomainInProcess() {
        return this == Locked || this == PreparingForMaintenance || this == Detaching || this == Activating;
    }
}
