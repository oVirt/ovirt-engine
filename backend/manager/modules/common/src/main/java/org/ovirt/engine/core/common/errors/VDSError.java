package org.ovirt.engine.core.common.errors;

import org.ovirt.engine.core.compat.Guid;

import java.util.ArrayList;

public class VDSError {

    private String message;
    private VdcBllErrors code;
    private ArrayList<Object> args;
    private Guid vdsId;

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

    public ArrayList<Object> getArgs() {
        return args;
    }

    public void setArgs(ArrayList<Object> value) {
        args = value;
    }

    public VDSError() {
        code = VdcBllErrors.forValue(0);
    }

    public Guid getVdsId() {
        return vdsId;
    }

    public void setVdsId(Guid vdsId) {
        this.vdsId = vdsId;
    }
}
