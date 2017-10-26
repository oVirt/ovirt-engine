package org.ovirt.engine.core.common.businessentities.storage;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ovirt.engine.core.common.businessentities.Identifiable;

public enum TransferType implements Identifiable {
    Unknown(0, "unknown"),
    Download(1, "read"),
    Upload(2, "write");

    private static final Map<String, TransferType> allowedOperationMapper;
    private static final Map<Integer, TransferType> valueToType;

    private int value;
    private String allowedOperation;

    static {
        allowedOperationMapper = Stream.of(values())
                .collect(Collectors.toMap(TransferType::getAllowedOperation, Function.identity()));
        valueToType = Stream.of(values())
                .collect(Collectors.toMap(TransferType::getValue, Function.identity()));
    }

    TransferType(int value, String allowedOperation) {
        this.value = value;
        this.allowedOperation = allowedOperation;
    }

    @Override
    public int getValue() {
        return value;
    }

    public static TransferType forValue(int value) {
        return valueToType.get(value);
    }

    public String getAllowedOperation() {
        return allowedOperation;
    }

    public static TransferType getTransferType(String allowedOperation) {
        return allowedOperationMapper.get(allowedOperation);
    }
}
