package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.ActivateStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public class ActivateStorageDomainVDSCommand<P extends ActivateStorageDomainVDSCommandParameters>
        extends IrsBrokerCommand<P> {
    private StorageStatusReturn result;

    public ActivateStorageDomainVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        result = getIrsProxy().activateStorageDomain(getParameters().getStorageDomainId().toString(),
                getParameters().getStoragePoolId().toString());
        proceedProxyReturnValue();
        setReturnValue(result.storageStatus);
    }

    @Override
    protected Status getReturnStatus() {
        return result.getStatus();
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return result;
    }
}
