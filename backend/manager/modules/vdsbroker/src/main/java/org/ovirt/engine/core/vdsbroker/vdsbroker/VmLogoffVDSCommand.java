package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VmLogoffVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;

public class VmLogoffVDSCommand<P extends VmLogoffVDSCommandParameters> extends VdsBrokerCommand<P> {
    private Guid mVmId = Guid.Empty;
    private boolean mForce;

    public VmLogoffVDSCommand(P parameters) {
        super(parameters);
        mVmId = parameters.getVmId();
        mForce = parameters.getForce();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().desktopLogoff(mVmId.toString(), String.valueOf(mForce));
        proceedProxyReturnValue();
    }
}
