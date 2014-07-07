package org.ovirt.engine.api.restapi.utils;

public class MappingException extends RuntimeException {

    public MappingException(String message, Throwable cause) {
        super(message, cause);
    }

    public MappingException(Throwable cause) {
        super(cause);
    }

    public MappingException(String message) {
        super(message);
    }
}
