package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.vdsbroker.*;
import org.ovirt.engine.core.common.vdscommands.*;

public class SPMGetTaskStatusVDSCommand<P extends SPMTaskGuidBaseVDSCommandParameters>
        extends SPMGetAllTasksStatusesVDSCommand<P> {
    public SPMGetTaskStatusVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        setReturnValue(ResourceManager
                .getInstance()
                .runVdsCommand(
                        VDSCommandType.HSMGetTaskStatus,
                        new HSMTaskGuidBaseVDSCommandParameters(getCurrentIrsProxyData().getCurrentVdsId(),
                                getParameters().getTaskId())).getReturnValue());
    }
}
