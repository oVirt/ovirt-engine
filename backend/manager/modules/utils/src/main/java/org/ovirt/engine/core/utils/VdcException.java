package org.ovirt.engine.core.utils;

public class VdcException extends RuntimeException {
    public VdcException() {
    }

    public VdcException(String message) {
        super(message);
    }

    public VdcException(Throwable cause) {
        super(cause);
    }

    public VdcException(String message, Throwable cause) {
        super(message, cause);
    }
}
