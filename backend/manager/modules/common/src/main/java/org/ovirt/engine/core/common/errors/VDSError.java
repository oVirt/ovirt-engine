package org.ovirt.engine.core.common.errors;

public class VDSError {

    private String message;
    private VdcBllErrors code;
    private java.util.ArrayList<Object> args;

    public VDSError(VdcBllErrors code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String value) {
        message = value;
    }


    public VdcBllErrors getCode() {
        return code;
    }

    public void setCode(VdcBllErrors value) {
        code = value;
    }

    public java.util.ArrayList<Object> getArgs() {
        return args;
    }

    public void setArgs(java.util.ArrayList<Object> value) {
        args = value;
    }

    public VDSError() {
        code = VdcBllErrors.forValue(0);
    }
}
