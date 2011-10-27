package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.*;

public class RemoveVGVDSCommand<P extends RemoveVGVDSCommandParameters> extends VdsBrokerCommand<P> {
    public RemoveVGVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status = getBroker().removeVG(getParameters().getVGID());
        ProceedProxyReturnValue();
    }
}
