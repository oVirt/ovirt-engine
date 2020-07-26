package org.ovirt.engine.core.common.businessentities.storage;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ovirt.engine.core.common.businessentities.Identifiable;

public enum TransferClientType implements Identifiable {
    UNKNOWN(0, "Unknown"),
    TRANSFER_VIA_BROWSER(1, "Transfer via browser"),
    TRANSFER_VIA_API(2, "Transfer via API");

    private int value;
    private String description;
    private static final Map<Integer, TransferClientType> valueToType = Stream.of(values())
            .collect(Collectors.toMap(TransferClientType::getValue, Function.identity()));

    TransferClientType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public int getValue() {
        return value;
    }

    public static TransferClientType forValue(int value) {
        return valueToType.get(value);
    }

    @Override
    public String toString() {
        return description;
    }

    public boolean isBrowserTransfer() {
        return this == TRANSFER_VIA_BROWSER;
    }
}
