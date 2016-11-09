package org.ovirt.engine.core.common.businessentities.storage;

public enum TransferType {
    Download("read"),
    Upload("write");

    private String op;

    TransferType(String op) {
        this.op = op;
    }

    public String getOp() {
        return op;
    }
}
