package org.ovirt.engine.core.common.errors;

import org.ovirt.engine.core.compat.*;

public class VdcBLLException extends ApplicationException implements java.io.Serializable {
    // protected VdcBLLException(SerializationInfo info, StreamingContext
    // context)
    // {
    // super(info, context);
    // VDSError tempVar = new VDSError();
    // tempVar.setCode((VdcBllErrors)info.GetValue("errorCode",
    // VdcBllErrors.class));
    // setVdsError(tempVar);
    // }

    private static final long serialVersionUID = 9070362191178977106L;

    public VdcBLLException(VdcBllErrors errCode, RuntimeException baseException) {
        super("VdcBLLException:", baseException);
        VDSError tempVar = new VDSError();
        tempVar.setCode(errCode);
        setVdsError(tempVar);
    }

    public VdcBLLException(VdcBllErrors errCode, String errorStr) {
        super("VdcBLLException: " + errorStr);
        VDSError tempVar = new VDSError();
        tempVar.setCode(errCode);
        setVdsError(tempVar);
    }

    public VdcBLLException(VdcBllErrors errCode) {
        super("VdcBLLException: " + errCode.toString());
        VDSError tempVar = new VDSError();
        tempVar.setCode(errCode);
        setVdsError(tempVar);
    }

    // @Override
    // public void GetObjectData(SerializationInfo info, StreamingContext
    // context)
    // {
    // info.AddValue("errorCode", getErrorCode());
    // super.GetObjectData(info, context);
    // }

    // void ISerializable.GetObjectData(SerializationInfo info, StreamingContext
    // context)
    // {
    // info.AddValue("errorCode", mErrorCode);
    // base.GetObjectData(info, context);
    // }

    private VDSError privateVdsError;

    public VDSError getVdsError() {
        return privateVdsError;
    }

    public void setVdsError(VDSError value) {
        privateVdsError = value;
    }

    public VdcBllErrors getErrorCode() {
        return getVdsError().getCode();
    }

    public VdcBLLException() {
    }

    @Override
    public String getMessage() {
        return String.format(super.getMessage() + " (Failed with VDSM error %1$s and code %2$s)",
                privateVdsError.getCode(),
                privateVdsError.getCode().getValue());
    }

}
