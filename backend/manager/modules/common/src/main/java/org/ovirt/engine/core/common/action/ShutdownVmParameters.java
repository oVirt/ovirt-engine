package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class ShutdownVmParameters extends StopVmParametersBase implements Serializable {
    private static final long serialVersionUID = 7007574816935458890L;
    private boolean waitBeforeShutdown;

    public ShutdownVmParameters() {
        waitBeforeShutdown = true;
    }

    public ShutdownVmParameters(Guid vmID, boolean waitBeforeShutdown) {
        this(vmID, waitBeforeShutdown, null);
    }

    public ShutdownVmParameters(Guid vmID, boolean waitBeforeShutdown, String reason) {
        super(vmID);
        this.waitBeforeShutdown = waitBeforeShutdown;
        setStopReason(reason);
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
