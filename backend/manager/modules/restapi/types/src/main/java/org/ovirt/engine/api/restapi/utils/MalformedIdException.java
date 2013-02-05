package org.ovirt.engine.api.restapi.utils;

public class MalformedIdException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public MalformedIdException(IllegalArgumentException e) {
        super(e);
    }
}
