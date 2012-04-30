package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;

public class ResetIrsVDSCommandParameters extends IrsBaseVDSCommandParameters {
    public ResetIrsVDSCommandParameters(Guid storagePoolId, String hostName, Guid vdsId) {
        super(storagePoolId);
        setVdsId(vdsId);
        _hostName = hostName;
    }

    private String _hostName;

    private Guid privateVdsId;

    public Guid getVdsId() {
        return privateVdsId;
    }

    public void setVdsId(Guid value) {
        privateVdsId = value;
    }

    public String getHostName() {
        return _hostName;
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
        return String.format("%s, hostName = %s, vdsId = %s, ignoreStopFailed = %s",
                super.toString(),
                getHostName(),
                getVdsId(),
                getIgnoreStopFailed());
    }
}
