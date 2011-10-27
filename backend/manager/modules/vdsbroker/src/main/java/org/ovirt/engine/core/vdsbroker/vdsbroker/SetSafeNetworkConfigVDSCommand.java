package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.*;

public class SetSafeNetworkConfigVDSCommand<P extends VdsIdVDSCommandParametersBase> extends VdsBrokerCommand<P> {
    public SetSafeNetworkConfigVDSCommand(P param) {
        super(param);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status = getBroker().setSafeNetworkConfig();
        ProceedProxyReturnValue();
    }
}
