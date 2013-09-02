package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.StorageServerConnectionManagementVDSParameters;

public class DisconnectStorageServerVDSCommand<P extends StorageServerConnectionManagementVDSParameters>
        extends ConnectStorageServerVDSCommand<P> {
    public DisconnectStorageServerVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        _result = getBroker().disconnectStorageServer(getParameters().getStorageType().getValue(),
                getParameters().getStoragePoolId().toString(), BuildStructFromConnectionListObject());
        proceedProxyReturnValue();
        setReturnValue(_result.convertToStatusList());
    }
}
