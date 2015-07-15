package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VmLogoffVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;

public class VmLogoffVDSCommand<P extends VmLogoffVDSCommandParameters> extends VdsBrokerCommand<P> {
    private Guid vmId = Guid.Empty;
    private boolean force;

    public VmLogoffVDSCommand(P parameters) {
        super(parameters);
        vmId = parameters.getVmId();
        force = parameters.getForce();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().desktopLogoff(vmId.toString(), String.valueOf(force));
        proceedProxyReturnValue();
    }
}
