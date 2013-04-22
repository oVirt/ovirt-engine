package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.vdscommands.*;

public class VmLogoffVDSCommand<P extends VmLogoffVDSCommandParameters> extends VdsBrokerCommand<P> {
    private Guid mVmId = new Guid();
    private boolean mForce;

    public VmLogoffVDSCommand(P parameters) {
        super(parameters);
        mVmId = parameters.getVmId();
        mForce = parameters.getForce();
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status = getBroker().desktopLogoff(mVmId.toString(), String.valueOf(mForce));
        ProceedProxyReturnValue();
    }
}
