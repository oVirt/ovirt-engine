package org.ovirt.engine.core.vdsbroker.xmlrpc;

import java.lang.reflect.UndeclaredThrowableException;

public class XmlRpcRunTimeException extends RuntimeException {

    public static final String NETWORK_ERROR_MSG = "I/O error while communicating with HTTP server: Connection refused";

    public XmlRpcRunTimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public XmlRpcRunTimeException(UndeclaredThrowableException cause) {
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
