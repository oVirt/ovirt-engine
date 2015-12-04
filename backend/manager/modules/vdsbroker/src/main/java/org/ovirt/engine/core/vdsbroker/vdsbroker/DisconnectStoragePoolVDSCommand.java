package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.DisconnectStoragePoolVDSCommandParameters;

public class DisconnectStoragePoolVDSCommand<P extends DisconnectStoragePoolVDSCommandParameters>
        extends VdsBrokerCommand<P> {
    public DisconnectStoragePoolVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().disconnectStoragePool(getParameters().getStoragePoolId().toString(),
                getParameters().getVdsSpmId(), getParameters().getStoragePoolId().toString());
        proceedProxyReturnValue();
    }
}
