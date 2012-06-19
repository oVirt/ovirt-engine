package org.ovirt.engine.core.compat;

/**
 * @deprecated Use org.apache.commons.lang.NotImplementedException instead.
 */
@Deprecated
public class NotImplementedException extends RuntimeException {

    public NotImplementedException() {
        super();
    }

    public NotImplementedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotImplementedException(String message) {
        super(message);
    }

    public NotImplementedException(Throwable cause) {
        super(cause);
    }
}
