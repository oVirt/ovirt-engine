package org.ovirt.engine.core.utils;

public class SerializationException extends RuntimeException {
    private static final long serialVersionUID = -5307210556111127692L;

    public SerializationException() {
    }

    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(Throwable cause) {
        super(cause);
    }

    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }

}
