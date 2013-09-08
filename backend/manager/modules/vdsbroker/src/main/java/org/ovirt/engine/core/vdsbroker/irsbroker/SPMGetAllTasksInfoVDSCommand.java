package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

public class SPMGetAllTasksInfoVDSCommand<P extends IrsBaseVDSCommandParameters> extends IrsBrokerCommand<P> {
    public SPMGetAllTasksInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        log.infoFormat(
                "-- executeIrsBrokerCommand: Attempting on storage pool '{0}'",
                getParameters().getStoragePoolId());

        setReturnValue(ResourceManager
                .getInstance()
                .runVdsCommand(VDSCommandType.HSMGetAllTasksInfo,
                        new VdsIdVDSCommandParametersBase(getCurrentIrsProxyData().getCurrentVdsId()))
                .getReturnValue());
    }

    private static Log log = LogFactory.getLog(SPMGetAllTasksInfoVDSCommand.class);
}
