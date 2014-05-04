package org.ovirt.engine.core.common.utils.exceptions;

/**
 * Exception marking failure in initialization flow
 *
 */
public class InitializationException extends Exception {

    public InitializationException() {
    }

    public InitializationException(String message) {
        super(message);
    }

    public InitializationException(Throwable cause) {
        super(cause);
    }

    public InitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
