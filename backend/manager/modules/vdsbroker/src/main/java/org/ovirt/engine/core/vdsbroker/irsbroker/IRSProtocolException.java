package org.ovirt.engine.core.vdsbroker.irsbroker;

public class IRSProtocolException extends IRSGenericException implements java.io.Serializable {
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
