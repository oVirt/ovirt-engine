package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class ResetIrsVDSCommandParameters extends IrsBaseVDSCommandParameters {

    private Guid privateVdsId;

    private Guid preferredSPMId;

    private boolean vdsAlreadyRebooted;

    public ResetIrsVDSCommandParameters() {
    }

    public ResetIrsVDSCommandParameters(Guid storagePoolId, Guid vdsId) {
        super(storagePoolId);
        setVdsId(vdsId);
    }

    public ResetIrsVDSCommandParameters(Guid storagePoolId, Guid vdsId, Guid preferredSPMId) {
        this(storagePoolId, vdsId);
        setPreferredSPMId(preferredSPMId);
    }

    public Guid getVdsId() {
        return privateVdsId;
    }

    public void setVdsId(Guid value) {
        privateVdsId = value;
    }

    public boolean isVdsAlreadyRebooted() {
        return vdsAlreadyRebooted;
    }

    public void setVdsAlreadyRebooted(boolean vdsAlreadyRebooted) {
        this.vdsAlreadyRebooted = vdsAlreadyRebooted;
    }

    private boolean privateIgnoreStopFailed;

    public boolean getIgnoreStopFailed() {
        return privateIgnoreStopFailed;
    }

    public Guid getPreferredSPMId() {
        return preferredSPMId;
    }

    public void setPreferredSPMId(Guid preferredSPMId) {
        this.preferredSPMId = preferredSPMId;
    }

    public void setIgnoreStopFailed(boolean value) {
        privateIgnoreStopFailed = value;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("vdsId", getVdsId())
                .append("ignoreStopFailed", getIgnoreStopFailed());
    }
}
