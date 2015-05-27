package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;

public abstract class ManagingVmCommand<P extends VdsAndVmIDVDSParametersBase> extends VDSCommandBase<P> {

    protected final VmManager vmManager;

    public ManagingVmCommand(P parameters) {
        super(parameters);
        vmManager = ResourceManager.getInstance().getVmManager(parameters.getVmId());
    }

    protected void executeVDSCommand() {
        vmManager.lock();
        try {
            executeVmCommand();
            updateVmDataChangedTime();
        } finally {
            vmManager.unlock();
        }
    }

    /**
     * update the time the vm data was changed
     */
    protected void updateVmDataChangedTime() {
        vmManager.updateVmDataChangedTime();
    }

    protected abstract void executeVmCommand();
}
