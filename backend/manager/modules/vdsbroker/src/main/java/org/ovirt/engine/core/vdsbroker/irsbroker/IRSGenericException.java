package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.io.Serializable;

import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSExceptionBase;

public class IRSGenericException extends VDSExceptionBase implements Serializable {
    // protected IRSGenericException(SerializationInfo info, StreamingContext
    // context)
    // {
    // super(info, context);
    // }
    public IRSGenericException(String message, RuntimeException baseException) {
        super(message, baseException);

    }

    public IRSGenericException(RuntimeException baseException) {
        super("IRSGenericException: ", baseException);
    }

    public IRSGenericException(String errorStr) {
        super("IRSGenericException: " + errorStr);

    }
}
