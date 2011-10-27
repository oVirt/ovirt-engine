package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.vdsbroker.*;
import org.ovirt.engine.core.common.asynctasks.*;
import org.ovirt.engine.core.common.vdscommands.*;

public class SPMGetAllTasksInfoVDSCommand<P extends IrsBaseVDSCommandParameters> extends IrsBrokerCommand<P> {
    public SPMGetAllTasksInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        if (getCurrentIrsProxyData().getCurrentVdsId().equals(Guid.Empty)) {
            setReturnValue(new java.util.ArrayList<AsyncTaskCreationInfo>());
        }

        else {
            log.infoFormat(
                    "-- SPMGetAllTasksInfoVDSCommand::ExecuteIrsBrokerCommand: Attempting on storage pool '{0}'",
                    getParameters().getStoragePoolId());

            setReturnValue(ResourceManager
                    .getInstance()
                    .runVdsCommand(VDSCommandType.HSMGetAllTasksInfo,
                            new VdsIdVDSCommandParametersBase(getCurrentIrsProxyData().getCurrentVdsId()))
                    .getReturnValue());
        }
    }

    private static LogCompat log = LogFactoryCompat.getLog(SPMGetAllTasksInfoVDSCommand.class);
}
