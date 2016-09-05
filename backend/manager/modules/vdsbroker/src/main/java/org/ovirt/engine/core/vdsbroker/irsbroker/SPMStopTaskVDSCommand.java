package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.HSMTaskGuidBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SPMTaskGuidBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

public class SPMStopTaskVDSCommand<P extends SPMTaskGuidBaseVDSCommandParameters> extends IrsBrokerCommand<P> {
    public SPMStopTaskVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        resourceManager.runVdsCommand(
                VDSCommandType.HSMStopTask,
                new HSMTaskGuidBaseVDSCommandParameters(getCurrentIrsProxy().getCurrentVdsId(),
                        getParameters().getTaskId()));
    }
}
