package org.ovirt.engine.core.vdsbroker.vdsbroker;

public class VDSNetworkException extends VDSGenericException implements java.io.Serializable {
    // protected VDSNetworkException(SerializationInfo info, StreamingContext
    // context)
    // {
    // super(info, context);
    // }
    public VDSNetworkException(RuntimeException baseException) {
        super("VDSNetworkException: ", baseException);
    }

    public VDSNetworkException(String errorStr) {
        super("VDSNetworkException: " + errorStr);

    }
}
