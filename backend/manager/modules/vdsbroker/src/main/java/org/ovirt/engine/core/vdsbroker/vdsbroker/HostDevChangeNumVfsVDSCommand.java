package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.HostDevChangeNumVfsVDSParameters;

public class HostDevChangeNumVfsVDSCommand<P extends HostDevChangeNumVfsVDSParameters> extends VdsBrokerCommand<P> {

    public HostDevChangeNumVfsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().hostdevChangeNumvfs(getParameters().getDeviceName(), getParameters().getNumOfVfs());
        proceedProxyReturnValue();
    }
}
