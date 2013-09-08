package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

@Logged(executionLevel = LogLevel.DEBUG)
public class SPMGetAllTasksStatusesVDSCommand<P extends IrsBaseVDSCommandParameters> extends IrsBrokerCommand<P> {
    public SPMGetAllTasksStatusesVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        setVDSReturnValue(ResourceManager.getInstance().runVdsCommand(VDSCommandType.HSMGetAllTasksStatuses,
                new VdsIdVDSCommandParametersBase(getCurrentIrsProxyData().getCurrentVdsId())));
    }
}
