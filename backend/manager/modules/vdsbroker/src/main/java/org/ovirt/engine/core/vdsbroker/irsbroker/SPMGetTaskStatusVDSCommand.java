package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.vdsbroker.*;
import org.ovirt.engine.core.common.vdscommands.*;

public class SPMGetTaskStatusVDSCommand<P extends SPMTaskGuidBaseVDSCommandParameters>
        extends SPMGetAllTasksStatusesVDSCommand<P> {
    public SPMGetTaskStatusVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        if (getCurrentIrsProxyData().getCurrentVdsId().equals(Guid.Empty)) {
            AsyncTaskStatus tempVar = new AsyncTaskStatus();
            tempVar.setMessage("No Host Available");
            setReturnValue(tempVar);
        } else {
            setReturnValue(ResourceManager
                    .getInstance()
                    .runVdsCommand(
                            VDSCommandType.HSMGetTaskStatus,
                            new HSMTaskGuidBaseVDSCommandParameters(getCurrentIrsProxyData().getCurrentVdsId(),
                                    getParameters().getTaskId())).getReturnValue());
        }
    }
}
