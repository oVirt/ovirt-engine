package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class ShutdownVmParameters extends VmOperationParameterBase implements java.io.Serializable {
    private static final long serialVersionUID = 7007574816935458890L;
    private boolean _waitBeforeShutdown;

    public ShutdownVmParameters() {
        _waitBeforeShutdown = true;
    }

    public ShutdownVmParameters(Guid vmID, boolean waitBeforeShutdown) {
        super(vmID);
        _waitBeforeShutdown = waitBeforeShutdown;
    }

    /**
     * If true: Before actually performing a shutdown within the guest, wait for a certain pertiod while an appropriate
     * message is displayed within the guest.
     */
    public boolean getWaitBeforeShutdown() {
        return _waitBeforeShutdown;
    }

    public void setWaitBeforeShutdown(boolean value) {
        _waitBeforeShutdown = value;
    }

}
