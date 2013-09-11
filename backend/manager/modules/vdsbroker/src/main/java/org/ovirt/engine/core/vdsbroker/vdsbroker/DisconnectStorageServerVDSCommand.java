package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.StorageServerConnectionManagementVDSParameters;

public class DisconnectStorageServerVDSCommand<P extends StorageServerConnectionManagementVDSParameters>
        extends ConnectStorageServerVDSCommand<P> {
    public DisconnectStorageServerVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        _result = getBroker().disconnectStorageServer(getParameters().getStorageType().getValue(),
                getParameters().getStoragePoolId().toString(), buildStructFromConnectionListObject());
        proceedProxyReturnValue();
        setReturnValue(_result.convertToStatusList());
    }
}
