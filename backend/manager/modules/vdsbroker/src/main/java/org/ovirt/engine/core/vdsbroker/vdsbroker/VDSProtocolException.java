package org.ovirt.engine.core.vdsbroker.vdsbroker;

public class VDSProtocolException extends VDSGenericException implements java.io.Serializable {
    // protected VDSProtocolException(SerializationInfo info, StreamingContext
    // context)
    // {
    // super(info, context);
    // }
    public VDSProtocolException(RuntimeException baseException) {
        super("VDSProtocolException: ", baseException);
    }

    public VDSProtocolException(String errorStr) {
        super("VDSProtocolException: " + errorStr);

    }
}
