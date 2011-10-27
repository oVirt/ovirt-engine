package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.vdsbroker.*;
import org.ovirt.engine.core.common.asynctasks.*;
import org.ovirt.engine.core.common.vdscommands.*;

public class SPMGetTaskInfoVDSCommand<P extends SPMTaskGuidBaseVDSCommandParameters>
        extends SPMGetAllTasksInfoVDSCommand<P> {
    public SPMGetTaskInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        if (getCurrentIrsProxyData().getCurrentVdsId().equals(Guid.Empty)) {
            setReturnValue(new AsyncTaskCreationInfo());
        } else {
            setReturnValue(ResourceManager
                    .getInstance()
                    .runVdsCommand(
                            VDSCommandType.HSMGetTaskInfo,
                            new HSMTaskGuidBaseVDSCommandParameters(getCurrentIrsProxyData().getCurrentVdsId(),
                                    getParameters().getTaskId())).getReturnValue());
        }
    }
}
