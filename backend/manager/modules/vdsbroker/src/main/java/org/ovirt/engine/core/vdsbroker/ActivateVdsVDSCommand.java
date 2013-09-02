package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.ActivateVdsVDSCommandParameters;

public class ActivateVdsVDSCommand<P extends ActivateVdsVDSCommandParameters> extends VdsIdVDSCommandBase<P> {
    public ActivateVdsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsIdCommand() {
        if (_vdsManager != null) {
            setReturnValue(_vdsManager.activate());
        } else {
            getVDSReturnValue().setSucceeded(false);
        }
    }
}
