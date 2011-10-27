package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.vdscommands.*;

public class VmLockVDSCommand<P extends VmLockVDSCommandParameters> extends VdsBrokerCommand<P> {
    private Guid mVmId = new Guid();

    public VmLockVDSCommand(P parameters) {
        super(parameters);
        mVmId = parameters.getVmId();
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status = getBroker().desktopLock(mVmId.toString());
        ProceedProxyReturnValue();
    }
}
