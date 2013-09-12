package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.io.Serializable;

public class VDSProtocolException extends VDSGenericException implements Serializable {
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
