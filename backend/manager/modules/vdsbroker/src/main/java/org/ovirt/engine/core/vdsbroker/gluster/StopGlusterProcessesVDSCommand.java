package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;

public class StopGlusterProcessesVDSCommand<P extends VdsIdVDSCommandParametersBase> extends AbstractGlusterBrokerCommand<P> {
    public StopGlusterProcessesVDSCommand(P params) {
        super(params);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().glusterStopProcesses();
        proceedProxyReturnValue();
    }
}
