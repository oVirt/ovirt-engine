package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.FenceSpmStorageVDSCommandParameters;

public class FenceSpmStorageVDSCommand<P extends FenceSpmStorageVDSCommandParameters> extends VdsBrokerCommand<P> {
    public FenceSpmStorageVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status = getBroker().fenceSpmStorage(getParameters().getStoragePoolId().toString(),
                getParameters().getPrevId(), getParameters().getPrevLVER());
        proceedProxyReturnValue();
    }
}
