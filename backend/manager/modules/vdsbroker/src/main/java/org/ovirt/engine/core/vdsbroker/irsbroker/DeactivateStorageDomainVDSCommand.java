package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.DeactivateStorageDomainVDSCommandParameters;

public class DeactivateStorageDomainVDSCommand<P extends DeactivateStorageDomainVDSCommandParameters>
        extends IrsBrokerCommand<P> {
    public DeactivateStorageDomainVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        status = getIrsProxy().deactivateStorageDomain(getParameters().getStorageDomainId().toString(),
                getParameters().getStoragePoolId().toString(),
                getParameters().getMasterStorageDomainId().toString(),
                getParameters().getMasterVersion());
        proceedProxyReturnValue();
    }
}
