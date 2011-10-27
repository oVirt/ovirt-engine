package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.vdscommands.*;

public class RunVmHyperChannelCommandVDSCommand<P extends RunVmHyperChannelCommandVDSCommandParameters>
        extends VdsBrokerCommand<P> {
    private Guid _vmId = new Guid();
    private String _hcCommand;

    public RunVmHyperChannelCommandVDSCommand(P parameters) {
        super(parameters);
        _vmId = parameters.getVmId();
        _hcCommand = parameters.getHcCommand();
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status = getBroker().sendHcCmdToDesktop(_vmId.toString(), _hcCommand);
        ProceedProxyReturnValue();
    }
}
