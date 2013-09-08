package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.RemoveGlusterServerVDSParameters;


public class RemoveGlusterServerVDSCommand<P extends RemoveGlusterServerVDSParameters> extends AbstractGlusterBrokerCommand<P> {

    public RemoveGlusterServerVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().glusterHostRemove(getParameters().getHostnameOrIp(), getParameters().isForceAction());

        proceedProxyReturnValue();
    }
}

