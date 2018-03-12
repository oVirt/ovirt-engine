package org.ovirt.engine.core.common.errors;

import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;

public class EngineException extends RuntimeException {

    private static final long serialVersionUID = 9070362191178977106L;

    private boolean useRootCause;

    public EngineException(EngineError errCode, RuntimeException baseException) {
        this(errCode, baseException, false);
    }

    public EngineException(EngineError errCode, Throwable baseException, boolean useRootCause) {
        super("EngineException:", baseException);
        this.useRootCause = useRootCause;
        VDSError tempVar = new VDSError();
        tempVar.setCode(errCode);
        setVdsError(tempVar);
    }

    public EngineException(EngineError errCode, String errorStr) {
        super("EngineException: " + errorStr);
        VDSError tempVar = new VDSError();
        tempVar.setCode(errCode);
        setVdsError(tempVar);
    }

    public EngineException(EngineError errCode) {
        super("EngineException: " + errCode.toString());
        VDSError tempVar = new VDSError();
        tempVar.setCode(errCode);
        setVdsError(tempVar);
    }

    public EngineException(EngineError errCode, String errorStr, VDSReturnValue vdsReturnValue) {
        this(errCode, errorStr);
        setVdsReturnValue(vdsReturnValue);
        final RuntimeException causedBy = vdsReturnValue.getExceptionObject();
        if (causedBy != null) {
            initCause(causedBy);
        }
    }

    private VDSError privateVdsError;

    public VDSError getVdsError() {
        return privateVdsError;
    }

    public void setVdsError(VDSError value) {
        privateVdsError = value;
    }

    public EngineError getErrorCode() {
        return getVdsError().getCode();
    }

    private VDSReturnValue vdsReturnValue;

    public VDSReturnValue getVdsReturnValue() {
        return vdsReturnValue;
    }

    public void setVdsReturnValue(VDSReturnValue vdsReturnValue) {
        this.vdsReturnValue = vdsReturnValue;
    }

    public boolean isUseRootCause() {
        return useRootCause;
    }

    public EngineException() {
    }

    @Override
    public String getMessage() {
        return String.format("%1$s (Failed with error %2$s and code %3$s)",
                super.getMessage(),
                useRootCause ? super.getCause().getMessage() : privateVdsError.getCode(),
                privateVdsError.getCode().getValue());
    }

}
