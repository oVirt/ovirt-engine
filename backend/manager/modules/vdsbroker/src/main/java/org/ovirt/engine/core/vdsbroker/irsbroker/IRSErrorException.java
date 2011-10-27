package org.ovirt.engine.core.vdsbroker.irsbroker;

public class IRSErrorException extends IRSGenericException implements java.io.Serializable {
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
