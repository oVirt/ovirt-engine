package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.ConnectStorageServerVDSCommandParameters;

public class DisconnectStorageServerVDSCommand<P extends ConnectStorageServerVDSCommandParameters>
        extends ConnectStorageServerVDSCommand<P> {
    public DisconnectStorageServerVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        _result = getBroker().disconnectStorageServer(getParameters().getStorageType().getValue(),
                getParameters().getStoragePoolId().toString(), BuildStructFromConnectionListObject());
        ProceedProxyReturnValue();
        setReturnValue(_result.convertToStatusList());
    }
}
