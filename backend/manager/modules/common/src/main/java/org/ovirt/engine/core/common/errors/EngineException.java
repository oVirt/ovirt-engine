package org.ovirt.engine.core.common.errors;

public class EngineException extends RuntimeException {

    private static final long serialVersionUID = 9070362191178977106L;

    public EngineException(EngineError errCode, RuntimeException baseException) {
        super("EngineException:", baseException);
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

    public EngineException() {
    }

    @Override
    public String getMessage() {
        return String.format(super.getMessage() + " (Failed with error %1$s and code %2$s)",
                privateVdsError.getCode(),
                privateVdsError.getCode().getValue());
    }

}
