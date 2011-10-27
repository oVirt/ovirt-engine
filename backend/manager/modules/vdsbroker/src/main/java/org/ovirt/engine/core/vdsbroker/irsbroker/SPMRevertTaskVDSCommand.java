package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.vdsbroker.*;
import org.ovirt.engine.core.common.vdscommands.*;

public class SPMRevertTaskVDSCommand<P extends SPMTaskGuidBaseVDSCommandParameters> extends IrsBrokerCommand<P> {
    public SPMRevertTaskVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        if (!getCurrentIrsProxyData().getCurrentVdsId().equals(Guid.Empty)) {
            VDSReturnValue returnValue =
                    ResourceManager.getInstance()
                                                        .runVdsCommand(
                                                                       VDSCommandType.HSMRevertTask,
                                                                       new HSMTaskGuidBaseVDSCommandParameters(getCurrentIrsProxyData().getCurrentVdsId(),
                                                                                                               getParameters().getTaskId()));
            if (returnValue != null && !returnValue.getSucceeded()) {
                getVDSReturnValue().setVdsError(returnValue.getVdsError());
                getVDSReturnValue().setSucceeded(false);
            }
        }
    }
}
