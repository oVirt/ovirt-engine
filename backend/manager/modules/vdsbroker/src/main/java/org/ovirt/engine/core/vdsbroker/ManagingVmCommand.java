package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;

public abstract class ManagingVmCommand<P extends VdsAndVmIDVDSParametersBase> extends VDSCommandBase<P> {

    protected VmManager vmManager;

    public ManagingVmCommand(P parameters) {
        super(parameters);
    }

    protected void executeVDSCommand() {
        vmManager = resourceManager.getVmManager(getParameters().getVmId());
        vmManager.lockVm();
        try {
            executeVmCommand();
            vmManager.updateVmDataChangedTime();
        } finally {
            vmManager.unlockVm();
        }
    }

    protected abstract void executeVmCommand();
}
