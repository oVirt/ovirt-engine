package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.CreateStorageDomainVDSCommandParameters;

public class CreateStorageDomainVDSCommand<P extends CreateStorageDomainVDSCommandParameters>
        extends VdsBrokerCommand<P> {
    public CreateStorageDomainVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().createStorageDomain(getParameters().getStorageDomain().getStorageType().getValue(),
                        getParameters().getStorageDomain().getId().toString(),
                        getParameters().getStorageDomain().getStorageName(),
                        getParameters().getArgs(),
                        getParameters().getStorageDomain().getStorageDomainType().getValue(),
                        getParameters().getStorageDomain().getStorageFormat().getValue());
        proceedProxyReturnValue();
    }
}
