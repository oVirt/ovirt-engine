package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class RebootVmParameters extends VmOperationParameterBase implements Serializable {
    private boolean forceStop;

    public RebootVmParameters() {
    }

    public RebootVmParameters(Guid vmId) {
        super(vmId);
    }

    public RebootVmParameters(Guid vmId, boolean forceStop) {
        this(vmId);
        setForceStop(forceStop);
    }

    public boolean isForceStop() {
        return forceStop;
    }

    public void setForceStop(boolean forceStop) {
        this.forceStop = forceStop;
    }
}
