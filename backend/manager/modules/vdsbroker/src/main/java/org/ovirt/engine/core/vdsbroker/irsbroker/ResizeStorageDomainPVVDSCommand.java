package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.ResizeStorageDomainPVVDSCommandParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.ResizeStorageDomainPVMapReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public class ResizeStorageDomainPVVDSCommand<P extends ResizeStorageDomainPVVDSCommandParameters>
        extends IrsBrokerCommand<P> {

    private ResizeStorageDomainPVMapReturn result;

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
    protected Status getReturnStatus() {
        return result.getStatus();
    }

}
