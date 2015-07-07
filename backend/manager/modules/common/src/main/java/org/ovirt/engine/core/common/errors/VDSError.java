package org.ovirt.engine.core.common.errors;

import java.util.ArrayList;

import org.ovirt.engine.core.compat.Guid;

public class VDSError {

    private String message;
    private EngineError code;
    private ArrayList<Object> args;
    private Guid vdsId;

    public VDSError(EngineError code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String value) {
        message = value;
    }


    public EngineError getCode() {
        return code;
    }

    public void setCode(EngineError value) {
        code = value;
    }

    public ArrayList<Object> getArgs() {
        return args;
    }

    public void setArgs(ArrayList<Object> value) {
        args = value;
    }

    public VDSError() {
        code = EngineError.forValue(0);
    }

    public Guid getVdsId() {
        return vdsId;
    }

    public void setVdsId(Guid vdsId) {
        this.vdsId = vdsId;
    }
}
