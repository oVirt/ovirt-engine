package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterWebhookVDSParameters;


public class SyncGlusterWebhookVDSCommand<P extends GlusterWebhookVDSParameters> extends AbstractGlusterBrokerCommand<P> {

    public SyncGlusterWebhookVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().glusterWebhookSync();

        proceedProxyReturnValue();
    }
}

