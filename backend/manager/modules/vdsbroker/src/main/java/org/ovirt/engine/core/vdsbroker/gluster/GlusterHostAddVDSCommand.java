package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterHostAddVDSParameters;

public class GlusterHostAddVDSCommand<P extends GlusterHostAddVDSParameters> extends AbstractGlusterBrokerCommand<P> {

    public GlusterHostAddVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status = getBroker().glusterHostAdd(getParameters().getHostName());
        ProceedProxyReturnValue();
    }
}
