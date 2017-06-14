package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Set;

import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.vdscommands.ExtendStorageDomainVDSCommandParameters;

public class ExtendStorageDomainVDSCommand<P extends ExtendStorageDomainVDSCommandParameters>
        extends IrsBrokerCommand<P> {
    public ExtendStorageDomainVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        String storageDomainId = getParameters().getStorageDomainId().toString();
        String storagePoolId = getParameters().getStoragePoolId().toString();
        Set<String> deviceList = getParameters().getDeviceList();
        String[] deviceArray = deviceList.toArray(new String[deviceList.size()]);
        boolean isForce = getParameters().isForce();

        status = getIrsProxy().extendStorageDomain(storageDomainId, storagePoolId, deviceArray, isForce);

        proceedProxyReturnValue();
    }

    @Override
    protected void proceedProxyReturnValue() {
        EngineError returnStatus = getReturnValueFromStatus(getReturnStatus());
        switch (returnStatus) {
        case DeviceNotFound:
            getVDSReturnValue().setSucceeded(false);
            break;
        default:
            super.proceedProxyReturnValue();
            initializeVdsError(returnStatus);
            break;
        }
    }
}
