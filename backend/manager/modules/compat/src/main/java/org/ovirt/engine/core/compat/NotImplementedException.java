package org.ovirt.engine.core.compat;

/**
 * @deprecated Use org.apache.commons.lang.NotImplementedException instead.
 */
@Deprecated
public class NotImplementedException extends RuntimeException {

    public NotImplementedException() {
        super();
    }

    public NotImplementedException(String message) {
        super(message);
    }

}
