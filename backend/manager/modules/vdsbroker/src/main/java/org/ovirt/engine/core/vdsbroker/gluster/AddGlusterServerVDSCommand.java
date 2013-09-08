package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.AddGlusterServerVDSParameters;


public class AddGlusterServerVDSCommand<P extends AddGlusterServerVDSParameters> extends AbstractGlusterBrokerCommand<P> {

    public AddGlusterServerVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().glusterHostAdd(getParameters().getHostnameOrIp());
        proceedProxyReturnValue();
    }
}
