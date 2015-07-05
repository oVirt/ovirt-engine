package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.VDSError;

public class VDSExceptionBase extends RuntimeException {

    private static final long serialVersionUID = 5709501011970689110L;

    private VDSError privateVdsError;

    public VDSError getVdsError() {
        return privateVdsError;
    }

    public void setVdsError(VDSError value) {
        privateVdsError = value;
    }

    public VDSExceptionBase(String errMessage, Throwable baseException) {
        super(errMessage, baseException);
    }

    public VDSExceptionBase(Throwable baseException) {
        super(baseException);
    }

    public VDSExceptionBase(String errMessage) {
        super(errMessage);
    }

    public VDSExceptionBase(EngineError errCode) {
        VDSError tempVar = new VDSError();
        tempVar.setCode(errCode);
        setVdsError(tempVar);
    }
}
