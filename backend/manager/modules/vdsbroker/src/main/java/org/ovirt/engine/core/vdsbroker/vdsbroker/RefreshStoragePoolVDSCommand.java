package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.*;

public class RefreshStoragePoolVDSCommand<P extends RefreshStoragePoolVDSCommandParameters>
        extends VdsBrokerCommand<P> {
    public RefreshStoragePoolVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status = getBroker()
                .refreshStoragePool(getParameters().getStoragePoolId().toString(),
                        getParameters().getMasterStorageDomainId().toString(),
                        getParameters().getMasterVersion());
        ProceedProxyReturnValue();
    }
}
