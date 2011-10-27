package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.vdsbroker.*;
import org.ovirt.engine.core.common.vdscommands.*;

public class SPMStopTaskVDSCommand<P extends SPMTaskGuidBaseVDSCommandParameters> extends IrsBrokerCommand<P> {
    public SPMStopTaskVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        if (!getCurrentIrsProxyData().getCurrentVdsId().equals(Guid.Empty)) {
            ResourceManager.getInstance().runVdsCommand(
                    VDSCommandType.HSMStopTask,
                    new HSMTaskGuidBaseVDSCommandParameters(getCurrentIrsProxyData().getCurrentVdsId(),
                            getParameters().getTaskId()));
        }
    }
}
