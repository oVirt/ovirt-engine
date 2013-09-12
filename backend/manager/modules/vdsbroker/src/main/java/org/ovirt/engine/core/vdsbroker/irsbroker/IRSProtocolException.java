package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.io.Serializable;

public class IRSProtocolException extends IRSGenericException implements Serializable {
    // protected IRSProtocolException(SerializationInfo info, StreamingContext
    // context)
    // {
    // super(info, context);
    // }
    public IRSProtocolException(RuntimeException baseException) {
        super("IRSProtocolException: ", baseException);
    }

    public IRSProtocolException(String errorStr) {
        super("IRSProtocolException: " + errorStr);

    }
}
