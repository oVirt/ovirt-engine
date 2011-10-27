package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.*;

public class ValidateStorageServerConnectionVDSCommand<P extends ConnectStorageServerVDSCommandParameters>
        extends ConnectStorageServerVDSCommand<P> {
    public ValidateStorageServerConnectionVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        _result = getBroker().validateStorageServerConnection(getParameters().getStorageType().getValue(),
                getParameters().getStoragePoolId().toString(), BuildStructFromConnectionListObject());
        ProceedProxyReturnValue();
        setReturnValue(GetStatusListFromResult());
    }
}
