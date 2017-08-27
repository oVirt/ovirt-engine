package org.ovirt.engine.core.common.businessentities.storage;

public enum TransferType {
    Download("read"),
    Upload("write");

    private String allowedOperation;

    TransferType(String allowedOperation) {
        this.allowedOperation = allowedOperation;
    }

    public String getAllowedOperation() {
        return allowedOperation;
    }
}
