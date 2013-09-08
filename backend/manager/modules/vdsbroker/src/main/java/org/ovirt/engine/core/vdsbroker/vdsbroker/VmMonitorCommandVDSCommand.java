package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VmMonitorCommandVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;

public class VmMonitorCommandVDSCommand<P extends VmMonitorCommandVDSCommandParameters> extends VdsBrokerCommand<P> {
    private Guid mVmId = Guid.Empty;
    private String mMonitorCommand;

    public VmMonitorCommandVDSCommand(P parameters) {
        super(parameters);
        mVmId = parameters.getVmId();
        mMonitorCommand = parameters.getCommand();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().monitorCommand(mVmId.toString(), mMonitorCommand);
        proceedProxyReturnValue();
    }
}
