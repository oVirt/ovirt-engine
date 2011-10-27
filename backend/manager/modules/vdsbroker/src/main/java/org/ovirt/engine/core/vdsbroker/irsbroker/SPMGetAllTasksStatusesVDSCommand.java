package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;
import org.ovirt.engine.core.vdsbroker.*;
import org.ovirt.engine.core.common.vdscommands.*;

@Logged(executionLevel = LogLevel.DEBUG)
public class SPMGetAllTasksStatusesVDSCommand<P extends IrsBaseVDSCommandParameters> extends IrsBrokerCommand<P> {
    public SPMGetAllTasksStatusesVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        if (getCurrentIrsProxyData().getCurrentVdsId().equals(Guid.Empty)) {
            setReturnValue(new java.util.HashMap<Guid, AsyncTaskStatus>());
        } else {
            setVDSReturnValue(ResourceManager.getInstance().runVdsCommand(VDSCommandType.HSMGetAllTasksStatuses,
                    new VdsIdVDSCommandParametersBase(getCurrentIrsProxyData().getCurrentVdsId())));
        }
    }
}
