package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterHostRemoveVDSParameters;

public class GlusterHostRemoveVDSCommand<P extends GlusterHostRemoveVDSParameters> extends AbstractGlusterBrokerCommand<P> {

    public GlusterHostRemoveVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status = getBroker().glusterHostRemove(getParameters().getHostName(), getParameters().isForceAction());

        ProceedProxyReturnValue();
    }
}

