package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.errors.VdcBllErrors;

public class VDSGenericException extends VDSExceptionBase implements java.io.Serializable {

    private static final long serialVersionUID = 4778043822136178263L;

    public VDSGenericException(String message, Throwable baseException) {
        super(message, baseException);
    }

    public VDSGenericException(Throwable baseException) {
        super(baseException);
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
