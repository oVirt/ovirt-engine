package org.ovirt.engine.core.vdsbroker;

import java.lang.reflect.UndeclaredThrowableException;

public class TransportRunTimeException extends RuntimeException {

    public static final String NETWORK_ERROR_MSG = "I/O error while communicating with HTTP server: Connection refused";

    public TransportRunTimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransportRunTimeException(UndeclaredThrowableException cause) {
        super(cause.getUndeclaredThrowable());
    }

    public boolean isNetworkError() {
        if (getMessage().startsWith(NETWORK_ERROR_MSG)) {
            return true;
        } else {
            return false;
        }
    }
}
