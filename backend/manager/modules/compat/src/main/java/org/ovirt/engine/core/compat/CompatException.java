package org.ovirt.engine.core.compat;

public class CompatException extends RuntimeException {
    public CompatException() {
        super();
    }

    public CompatException(String message) {
        super(message);
    }

    public CompatException(String message, Throwable cause) {
        super(message, cause);
    }

    public CompatException(Throwable cause) {
        super(cause);
    }
}
