package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.UpdateVmDynamicDataVDSCommandParameters;

public class UpdateVmDynamicDataVDSCommand<P extends UpdateVmDynamicDataVDSCommandParameters> extends ManagingVmCommand<P> {
    public UpdateVmDynamicDataVDSCommand(P parameters) {
        super(parameters);
    }

    @Override protected void executeVmCommand() {
        vmManager.update(getParameters().getVmDynamic());
        getVDSReturnValue().setSucceeded(true);
    }
}
