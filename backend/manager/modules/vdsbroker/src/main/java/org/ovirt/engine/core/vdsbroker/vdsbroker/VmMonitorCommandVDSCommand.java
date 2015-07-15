package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VmMonitorCommandVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;

public class VmMonitorCommandVDSCommand<P extends VmMonitorCommandVDSCommandParameters> extends VdsBrokerCommand<P> {
    private Guid vmId = Guid.Empty;
    private String monitorCommand;

    public VmMonitorCommandVDSCommand(P parameters) {
        super(parameters);
        vmId = parameters.getVmId();
        monitorCommand = parameters.getCommand();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().monitorCommand(vmId.toString(), monitorCommand);
        proceedProxyReturnValue();
    }
}
