package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.errors.*;

public class VDSGenericException extends VDSExceptionBase implements java.io.Serializable {
    // protected VDSGenericException(SerializationInfo info, StreamingContext
    // context)
    // {
    // super(info, context);
    // }
    public VDSGenericException(String message, RuntimeException baseException) {
        super(message, baseException);
    }

    public VDSGenericException(RuntimeException baseException) {
        super("VDSGenericException: ", baseException);
    }

    public VDSGenericException(String errorStr) {
        super("VDSGenericException: " + errorStr);

    }

    public VDSGenericException(VdcBllErrors errorCode) {
        super(errorCode);
    }

    public VDSGenericException(VdcBllErrors errorCode, String errorStr) {
        super(errorStr);
        VDSError tempVar = new VDSError();
        tempVar.setCode(errorCode);
        setVdsError(tempVar);
    }
}
