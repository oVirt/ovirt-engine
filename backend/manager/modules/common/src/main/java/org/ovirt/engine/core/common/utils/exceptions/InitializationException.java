package org.ovirt.engine.core.common.utils.exceptions;

/**
 * Exception marking failure in initialization flow
 *
 */
public class InitializationException extends Exception {

    private static final long serialVersionUID = 5575459819696919858L;

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
