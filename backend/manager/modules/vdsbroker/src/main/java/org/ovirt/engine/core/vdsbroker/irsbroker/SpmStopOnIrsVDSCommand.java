package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.ResetIrsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

public class SpmStopOnIrsVDSCommand<P extends SpmStopOnIrsVDSCommandParameters> extends IrsBrokerCommand<P> {
    public SpmStopOnIrsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        ResourceManager.getInstance().runVdsCommand(
                VDSCommandType.ResetIrs,
                new ResetIrsVDSCommandParameters(getParameters().getStoragePoolId(),
                        getCurrentIrsProxyData().getCurrentVdsId(), getParameters().getPreferredSPMId()));
    }
}
