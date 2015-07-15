package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.ActivateStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;

public class ActivateStorageDomainVDSCommand<P extends ActivateStorageDomainVDSCommandParameters>
        extends IrsBrokerCommand<P> {
    private StorageStatusReturnForXmlRpc result;

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
    protected StatusForXmlRpc getReturnStatus() {
        return result.getXmlRpcStatus();
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return result;
    }
}
