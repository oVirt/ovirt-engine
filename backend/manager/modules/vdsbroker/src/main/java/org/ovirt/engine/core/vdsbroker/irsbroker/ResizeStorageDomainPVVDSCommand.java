package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.ResizeStorageDomainPVVDSCommandParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.ResizeStorageDomainPVMapReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;

public class ResizeStorageDomainPVVDSCommand<P extends ResizeStorageDomainPVVDSCommandParameters>
        extends IrsBrokerCommand<P> {

    private ResizeStorageDomainPVMapReturnForXmlRpc result;

    public ResizeStorageDomainPVVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        String storageDomainId = getParameters().getStorageDomainId().toString();
        String storagePoolId = getParameters().getStoragePoolId().toString();
        String device = getParameters().getDevice();

        result = getIrsProxy().resizeStorageDomainPV(storageDomainId, storagePoolId, device);
        proceedProxyReturnValue();
        setReturnValue(result.getDeviceSize());
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return result;
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return result.getXmlRpcStatus();
    }

}
