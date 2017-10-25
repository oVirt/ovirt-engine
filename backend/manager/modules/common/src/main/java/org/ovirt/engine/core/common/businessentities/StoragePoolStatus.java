package org.ovirt.engine.core.common.businessentities;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum StoragePoolStatus implements Identifiable {
    Uninitialized(0),
    Up(1),
    Maintenance(2),
    NotOperational(3),
    NonResponsive(4),
    Contend(5);

    private int value;
    private static final Map<Integer, StoragePoolStatus> valueToStatus =
            Stream.of(values()).collect(Collectors.toMap(StoragePoolStatus::getValue, Function.identity()));

    StoragePoolStatus(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    public static StoragePoolStatus forValue(int value) {
        return valueToStatus.get(value);
    }
}
