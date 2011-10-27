package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.errors.*;

import org.ovirt.engine.core.common.vdscommands.*;

public class ExtendStorageDomainVDSCommand<P extends ExtendStorageDomainVDSCommandParameters>
        extends IrsBrokerCommand<P> {
    public ExtendStorageDomainVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        status = getIrsProxy().extendStorageDomain(getParameters().getStorageDomainId().toString(),
                getParameters().getStoragePoolId().toString(),
                getParameters().getDeviceList().toArray(new String[] {}));
        ProceedProxyReturnValue();
    }

    @Override
    protected void ProceedProxyReturnValue() {
        VdcBllErrors returnStatus = GetReturnValueFromStatus(getReturnStatus());
        switch (returnStatus) {
        case DeviceNotFound:
            getVDSReturnValue().setSucceeded(false);
            break;
        default:
            super.ProceedProxyReturnValue();
            InitializeVdsError(returnStatus);
            break;
        }
    }
}
