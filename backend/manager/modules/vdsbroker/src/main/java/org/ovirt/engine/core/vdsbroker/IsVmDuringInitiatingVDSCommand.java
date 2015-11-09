package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.IsVmDuringInitiatingVDSCommandParameters;

public class IsVmDuringInitiatingVDSCommand<P extends IsVmDuringInitiatingVDSCommandParameters>
extends VDSCommandBase<P> {
    public IsVmDuringInitiatingVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVDSCommand() {
        getVDSReturnValue().setReturnValue(resourceManager.isVmDuringInitiating(getParameters().getVmId()));
    }
}
