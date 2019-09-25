package org.ovirt.engine.core.common.utils.ansible;

public class PlaybookExecutionException extends RuntimeException {

    public PlaybookExecutionException(String message, String reason) {
        super(String.format(message, reason));
    }

}
