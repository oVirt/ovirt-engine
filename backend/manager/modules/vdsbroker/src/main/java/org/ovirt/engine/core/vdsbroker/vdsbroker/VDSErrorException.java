package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.errors.VdcBllErrors;

public class VDSErrorException extends VDSGenericException implements java.io.Serializable {
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

    public VDSErrorException(VdcBllErrors errorCode) {
        super(errorCode);
    }

    public VDSErrorException(VdcBllErrors errorCode, String errorStr) {
        super(errorCode, errorStr);
    }
}
