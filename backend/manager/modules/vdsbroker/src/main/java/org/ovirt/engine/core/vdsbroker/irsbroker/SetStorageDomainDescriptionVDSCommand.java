package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.SetStorageDomainDescriptionVDSCommandParameters;

public class SetStorageDomainDescriptionVDSCommand<P extends SetStorageDomainDescriptionVDSCommandParameters>
        extends IrsBrokerCommand<P> {
    public SetStorageDomainDescriptionVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        status = getIrsProxy().setStorageDomainDescription(getParameters().getStorageDomainId().toString(),
                getParameters().getDescription());
        proceedProxyReturnValue();
    }
}
