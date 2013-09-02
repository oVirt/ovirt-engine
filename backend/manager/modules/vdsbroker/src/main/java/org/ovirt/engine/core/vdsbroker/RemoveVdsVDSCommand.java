package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.RemoveVdsVDSCommandParameters;

public class RemoveVdsVDSCommand<P extends RemoveVdsVDSCommandParameters> extends VdsIdVDSCommandBase<P> {
    public RemoveVdsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsIdCommand() {
        ResourceManager.getInstance().RemoveVds(getVdsId());
    }
}
