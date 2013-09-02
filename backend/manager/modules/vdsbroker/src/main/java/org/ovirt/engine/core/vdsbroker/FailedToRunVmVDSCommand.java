package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.FailedToRunVmVDSCommandParameters;

public class FailedToRunVmVDSCommand<P extends FailedToRunVmVDSCommandParameters> extends VdsIdVDSCommandBase<P> {
    public FailedToRunVmVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsIdCommand() {
        if (_vdsManager != null) {
            _vdsManager.failedToRunVm(getVds());
        } else {
            getVDSReturnValue().setSucceeded(false);
        }
    }
}
