package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.vdsbroker.*;
import org.ovirt.engine.core.common.vdscommands.*;

public class SpmStopOnIrsVDSCommand<P extends IrsBaseVDSCommandParameters> extends IrsBrokerCommand<P> {
    public SpmStopOnIrsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        ResourceManager.getInstance().runVdsCommand(
                VDSCommandType.ResetIrs,
                new ResetIrsVDSCommandParameters(getParameters().getStoragePoolId(),
                        getCurrentIrsProxyData().getmCurrentIrsHost(), getCurrentIrsProxyData().getCurrentVdsId()));
    }
}
