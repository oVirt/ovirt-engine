package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class SshHostRebootParameters extends VdsActionParameters {
    private static final long serialVersionUID = 3959485593675384542L;

    /**
     * Determines, if waiting to finish reboot of the host should be executed synchronously.
     */
    protected boolean waitOnRebootSynchronous;

    public SshHostRebootParameters() {
        waitOnRebootSynchronous = false;
    }

    public SshHostRebootParameters(Guid hostId) {
        super(hostId);
        waitOnRebootSynchronous = false;
    }

    public boolean isWaitOnRebootSynchronous() {
        return waitOnRebootSynchronous;
    }

    public void setWaitOnRebootSynchronous(boolean waitOnRebootSynchronous) {
        this.waitOnRebootSynchronous = waitOnRebootSynchronous;
    }
}
