package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class ShutdownVmParameters extends StopVmParametersBase implements Serializable {
    private static final long serialVersionUID = 7007574816935458890L;
    private boolean waitBeforeShutdown;

    public ShutdownVmParameters() {
        waitBeforeShutdown = true;
    }

    public ShutdownVmParameters(Guid vmId, boolean waitBeforeShutdown) {
        super(vmId);
        this.waitBeforeShutdown = waitBeforeShutdown;
    }

    public ShutdownVmParameters(Guid vmId, boolean waitBeforeShutdown, String reason) {
        this(vmId, waitBeforeShutdown);
        setStopReason(reason);
    }

    public ShutdownVmParameters(Guid vmId, boolean waitBeforeShutdown, String reason, boolean forceStop) {
        this(vmId, waitBeforeShutdown, reason);
        setForceStop(forceStop);
    }

    /**
     * If true: Before actually performing a shutdown within the guest, wait for a certain pertiod while an appropriate
     * message is displayed within the guest.
     */
    public boolean getWaitBeforeShutdown() {
        return waitBeforeShutdown;
    }

    public void setWaitBeforeShutdown(boolean value) {
        waitBeforeShutdown = value;
    }

}
