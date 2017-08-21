package org.ovirt.engine.core.common.businessentities.storage;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum TransferType {
    Download("read"),
    Upload("write");

    private static final Map<String, TransferType> allowedOperationMapper;

    private String allowedOperation;

    static {
        allowedOperationMapper = Stream.of(values())
                .collect(Collectors.toMap(TransferType::getAllowedOperation, Function.identity()));
    }

    TransferType(String allowedOperation) {
        this.allowedOperation = allowedOperation;
    }

    public String getAllowedOperation() {
        return allowedOperation;
    }

    public static TransferType getTransferType(String allowedOperation) {
        return allowedOperationMapper.get(allowedOperation);
    }
}
