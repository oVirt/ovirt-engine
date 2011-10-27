package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.*;

public class AttachStorageDomainVDSCommand<P extends AttachStorageDomainVDSCommandParameters>
        extends IrsBrokerCommand<P> {
    public AttachStorageDomainVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        status = getIrsProxy().attachStorageDomain(getParameters().getStorageDomainId().toString(),
                getParameters().getStoragePoolId().toString());
        ProceedProxyReturnValue();
    }
}
