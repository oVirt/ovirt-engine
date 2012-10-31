package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.vdsbroker.*;
import org.ovirt.engine.core.common.vdscommands.*;

public class SPMStopTaskVDSCommand<P extends SPMTaskGuidBaseVDSCommandParameters> extends IrsBrokerCommand<P> {
    public SPMStopTaskVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        ResourceManager.getInstance().runVdsCommand(
                VDSCommandType.HSMStopTask,
                new HSMTaskGuidBaseVDSCommandParameters(getCurrentIrsProxyData().getCurrentVdsId(),
                        getParameters().getTaskId()));
    }
}
