package org.ovirt.engine.core.vdsbroker.irsbroker;

public class IRSNetworkException extends IRSGenericException implements java.io.Serializable {
    // protected IRSNetworkException(SerializationInfo info, StreamingContext
    // context)
    // {
    // super(info, context);
    // }
    public IRSNetworkException(RuntimeException baseException) {
        super("IRSNetworkException: ", baseException);
    }

    public IRSNetworkException(String errorStr) {
        super("IRSNetworkException: " + errorStr);

    }
}
