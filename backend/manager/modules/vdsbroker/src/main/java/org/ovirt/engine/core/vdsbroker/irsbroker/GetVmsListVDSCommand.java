package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.vdsbroker.vdsbroker.*;
import org.ovirt.engine.core.common.vdscommands.*;

public class GetVmsListVDSCommand<P extends GetVmsInfoVDSCommandParameters> extends IrsBrokerCommand<P> {
    private GetVmsListReturnForXmlRpc _vmsList;

    public GetVmsListVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        String storagePoolId = getParameters().getStoragePoolId().toString();
        String storageDomainId = getParameters().getStorageDomainId().toString();

        _vmsList = getIrsProxy().getVmsList(storagePoolId, storageDomainId);
        ProceedProxyReturnValue();

        setReturnValue(_vmsList.vmlist);
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _vmsList.mStatus;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _vmsList;
    }
}
