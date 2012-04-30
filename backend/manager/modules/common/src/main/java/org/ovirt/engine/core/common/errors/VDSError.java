package org.ovirt.engine.core.common.errors;

public class VDSError {

    public VDSError(VdcBllErrors code, String message) {
        this.privateCode = code;
        this.privateMessage = message;
    }

    private String privateMessage;

    public String getMessage() {
        return privateMessage;
    }

    public void setMessage(String value) {
        privateMessage = value;
    }

    private VdcBllErrors privateCode = VdcBllErrors.forValue(0);

    public VdcBllErrors getCode() {
        return privateCode;
    }

    public void setCode(VdcBllErrors value) {
        privateCode = value;
    }

    private java.util.ArrayList<Object> privateArgs;

    public java.util.ArrayList<Object> getArgs() {
        return privateArgs;
    }

    public void setArgs(java.util.ArrayList<Object> value) {
        privateArgs = value;
    }

    public VDSError() {
    }
}
