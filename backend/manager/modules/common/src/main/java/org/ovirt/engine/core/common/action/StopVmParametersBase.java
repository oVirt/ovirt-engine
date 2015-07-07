package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public abstract class StopVmParametersBase extends VmOperationParameterBase implements Serializable {

    public StopVmParametersBase() {
    }

    public StopVmParametersBase(Guid vmId) {
        super(vmId);
    }

    private String stopReason;

    public String getStopReason() {
        return stopReason;
    }

    public void setStopReason(String value) {
        stopReason = value;
    }
}
