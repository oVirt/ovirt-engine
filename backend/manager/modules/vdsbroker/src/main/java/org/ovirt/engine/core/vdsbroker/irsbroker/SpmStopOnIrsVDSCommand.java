package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.ResetIrsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

public class SpmStopOnIrsVDSCommand<P extends SpmStopOnIrsVDSCommandParameters> extends IrsBrokerCommand<P> {
    public SpmStopOnIrsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        getVDSReturnValue().setSucceeded(resourceManager
                .runVdsCommand(
                        VDSCommandType.ResetIrs,
                        new ResetIrsVDSCommandParameters(getParameters().getStoragePoolId(),
                                getCurrentIrsProxy().getCurrentVdsId(), getParameters().getPreferredSPMId()))
                .getSucceeded());
    }
}
