package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.RemoveVdsVDSCommandParameters;

public class RemoveVdsVDSCommand<P extends RemoveVdsVDSCommandParameters> extends VdsIdVDSCommandBase<P> {

    private P parameters;

    public RemoveVdsVDSCommand(P parameters) {
        super(parameters, parameters.isNewHost());
        this.parameters = parameters;
    }

    @Override
    protected void executeVdsIdCommand() {
        resourceManager.removeVds(getVdsId(), parameters.isNewHost());
    }
}
