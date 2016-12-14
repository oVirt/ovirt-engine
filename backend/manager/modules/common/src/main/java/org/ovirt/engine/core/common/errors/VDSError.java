package org.ovirt.engine.core.common.errors;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class VDSError {

    private String message;
    private EngineError code;
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

    public VDSError() {
        code = EngineError.forValue(0);
    }

    public Guid getVdsId() {
        return vdsId;
    }

    public void setVdsId(Guid vdsId) {
        this.vdsId = vdsId;
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("code", getCode())
                .append("message", getMessage())
                .build();
    }
}
