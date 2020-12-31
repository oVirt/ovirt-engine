package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public abstract class StopVmParametersBase extends VmOperationParameterBase implements Serializable {
    private String stopReason;
    private boolean forceStop;

    public StopVmParametersBase() {
    }

    public StopVmParametersBase(Guid vmId) {
        super(vmId);
    }

    public String getStopReason() {
        return stopReason;
    }

    public void setStopReason(String stopReason) {
        this.stopReason = stopReason;
    }

    public boolean isForceStop() {
        return forceStop;
    }

    public void setForceStop(boolean forceStop) {
        this.forceStop = forceStop;
    }
}
