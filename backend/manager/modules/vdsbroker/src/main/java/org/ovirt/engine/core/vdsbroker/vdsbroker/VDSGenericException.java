package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.io.Serializable;

import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.VDSError;

public class VDSGenericException extends VDSExceptionBase implements Serializable {

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

    public VDSGenericException(EngineError errorCode) {
        super(errorCode);
    }

    public VDSGenericException(EngineError errorCode, String errorStr) {
        super(errorStr);
        VDSError tempVar = new VDSError();
        tempVar.setCode(errorCode);
        setVdsError(tempVar);
    }
}
