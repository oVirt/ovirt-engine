package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.SetStoragePoolDescriptionVDSCommandParameters;

public class SetStoragePoolDescriptionVDSCommand<P extends SetStoragePoolDescriptionVDSCommandParameters>
        extends IrsBrokerCommand<P> {
    public SetStoragePoolDescriptionVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        status = getIrsProxy().setStoragePoolDescription(getParameters().getStoragePoolId().toString(),
                getParameters().getDescription());
        proceedProxyReturnValue();
    }
}
