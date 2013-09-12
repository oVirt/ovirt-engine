package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.io.Serializable;

public class IRSErrorException extends IRSGenericException implements Serializable {
    // protected IRSErrorException(SerializationInfo info, StreamingContext
    // context)
    // {
    // super(info, context);
    // }
    public IRSErrorException(RuntimeException baseException) {
        super("IRSErrorException: ", baseException);
    }

    public IRSErrorException(String errorStr) {
        super("IRSErrorException: " + errorStr);

    }
}
