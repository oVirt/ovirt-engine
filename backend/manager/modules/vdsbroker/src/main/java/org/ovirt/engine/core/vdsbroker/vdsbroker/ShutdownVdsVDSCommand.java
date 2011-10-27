package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;

public class ShutdownVdsVDSCommand<P extends ShutdownVdsVDSCommandParameters> extends VdsBrokerCommand<P> {
    public ShutdownVdsVDSCommand(P parameters) {
        super(parameters);
        _reboot = parameters.getReboot() ? 1 : 0;
    }

    private int _reboot;

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status = getBroker().shutdownHost(_reboot);
        ProceedProxyReturnValue();
        this.setReturnValue(VDSStatus.Down);
    }
}
