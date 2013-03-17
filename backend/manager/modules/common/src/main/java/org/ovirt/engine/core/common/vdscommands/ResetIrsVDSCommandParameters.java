package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class ResetIrsVDSCommandParameters extends IrsBaseVDSCommandParameters {
    public ResetIrsVDSCommandParameters(Guid storagePoolId, Guid vdsId) {
        super(storagePoolId);
        setVdsId(vdsId);
    }

    private Guid privateVdsId;

    public Guid getVdsId() {
        return privateVdsId;
    }

    public void setVdsId(Guid value) {
        privateVdsId = value;
    }

    private boolean privateIgnoreStopFailed;

    public boolean getIgnoreStopFailed() {
        return privateIgnoreStopFailed;
    }

    public void setIgnoreStopFailed(boolean value) {
        privateIgnoreStopFailed = value;
    }

    public ResetIrsVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, vdsId = %s, ignoreStopFailed = %s",
                super.toString(),
                getVdsId(),
                getIgnoreStopFailed());
    }
}
