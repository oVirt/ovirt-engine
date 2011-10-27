package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.vdsbroker.*;
import org.ovirt.engine.core.common.vdscommands.*;

public class SPMClearTaskVDSCommand<P extends SPMTaskGuidBaseVDSCommandParameters>
        extends IrsBrokerCommand<P> {
    public SPMClearTaskVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        if (!getCurrentIrsProxyData().getCurrentVdsId().equals(Guid.Empty)) {
            VDSReturnValue returnValue = ResourceManager.getInstance().runVdsCommand(
                    VDSCommandType.HSMClearTask,
                    new HSMTaskGuidBaseVDSCommandParameters(getCurrentIrsProxyData().getCurrentVdsId(),
                            getParameters().getTaskId()));
            if (returnValue != null && !returnValue.getSucceeded()) {
                getVDSReturnValue().setVdsError(returnValue.getVdsError());
                getVDSReturnValue().setSucceeded(false);
            }
        }
    }
}
