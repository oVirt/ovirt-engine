package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.HSMTaskGuidBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SPMTaskGuidBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;

public class SPMRevertTaskVDSCommand<P extends SPMTaskGuidBaseVDSCommandParameters> extends IrsBrokerCommand<P> {
    public SPMRevertTaskVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        VDSReturnValue returnValue =
                resourceManager.runVdsCommand(
                        VDSCommandType.HSMRevertTask,
                        new HSMTaskGuidBaseVDSCommandParameters(getCurrentIrsProxy().getCurrentVdsId(),
                                getParameters().getTaskId()));
        if (returnValue != null && !returnValue.getSucceeded()) {
            getVDSReturnValue().setVdsError(returnValue.getVdsError());
            getVDSReturnValue().setSucceeded(false);
        }
    }
}
