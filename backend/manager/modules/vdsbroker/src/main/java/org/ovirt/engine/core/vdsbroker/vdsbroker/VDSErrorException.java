package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.io.Serializable;

import org.ovirt.engine.core.common.errors.EngineError;

public class VDSErrorException extends VDSGenericException implements Serializable {
    // protected VDSErrorException(SerializationInfo info, StreamingContext
    // context)
    // {
    // super(info, context);
    // }
    public VDSErrorException(RuntimeException baseException) {
        super("VDSErrorException: ", baseException);
    }

    public VDSErrorException(String errorStr) {
        super("VDSErrorException: " + errorStr);

    }

    public VDSErrorException(EngineError errorCode) {
        super(errorCode);
    }

    public VDSErrorException(EngineError errorCode, String errorStr) {
        super(errorCode, errorStr);
    }
}
