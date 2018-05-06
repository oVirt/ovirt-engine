package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class DestroyVmVDSCommandParameters extends VdsAndVmIDVDSParametersBase {

    private boolean gracefully;
    private int secondsToWait;
    private String reason;
    private boolean ignoreNoVm;

    public DestroyVmVDSCommandParameters() {
    }

    public DestroyVmVDSCommandParameters(Guid vdsId, Guid vmId) {
        super(vdsId, vmId);
    }

    public void setSecondsToWait(int secondsToWait) {
        this.secondsToWait = secondsToWait;
    }

    public int getSecondsToWait() {
        return secondsToWait;
    }

    public void setGracefully(boolean gracefully) {
        this.gracefully = gracefully;
    }

    public boolean getGracefully() {
        return gracefully;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason == null ? "" : reason;
    }

    public void setIgnoreNoVm(boolean ignoreNoVm) {
        this.ignoreNoVm = ignoreNoVm;
    }

    public boolean isIgnoreNoVm() {
        return ignoreNoVm;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("secondsToWait", getSecondsToWait())
                .append("gracefully", getGracefully())
                .append("reason", getReason())
                .append("ignoreNoVm", isIgnoreNoVm());
    }
}
