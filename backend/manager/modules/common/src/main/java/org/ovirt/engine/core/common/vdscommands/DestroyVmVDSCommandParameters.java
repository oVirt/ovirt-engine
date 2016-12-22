package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class DestroyVmVDSCommandParameters extends VdsAndVmIDVDSParametersBase {

    private boolean gracefully;
    private int secondsToWait;
    private String reason;
    private boolean ignoreNoVm;

    public DestroyVmVDSCommandParameters(Guid vdsId, Guid vmId, boolean gracefully, int secondsToWait) {
        this(vdsId, vmId, null, gracefully, secondsToWait);
    }

    public DestroyVmVDSCommandParameters(Guid vdsId, Guid vmId, String reason, boolean gracefully, int secondsToWait, boolean ignoreNoVm) {
        super(vdsId, vmId);
        this.gracefully = gracefully;
        this.secondsToWait = secondsToWait;
        this.reason = reason;
        this.ignoreNoVm = ignoreNoVm;
    }

    public DestroyVmVDSCommandParameters(Guid vdsId, Guid vmId, String reason, boolean gracefully, int secondsToWait) {
        this(vdsId, vmId, reason, gracefully, secondsToWait, false);
    }

    public int getSecondsToWait() {
        return secondsToWait;
    }

    public boolean getGracefully() {
        return gracefully;
    }

    public String getReason() {
        return reason == null ? "" : reason;
    }

    public boolean isIgnoreNoVm() {
        return ignoreNoVm;
    }

    public DestroyVmVDSCommandParameters() {
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
