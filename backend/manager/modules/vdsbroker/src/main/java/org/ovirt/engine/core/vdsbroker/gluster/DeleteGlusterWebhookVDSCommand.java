package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterWebhookVDSParameters;


public class DeleteGlusterWebhookVDSCommand<P extends GlusterWebhookVDSParameters> extends AbstractGlusterBrokerCommand<P> {

    public DeleteGlusterWebhookVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().glusterWebhookDelete(getParameters().getWebhookUrl());

        proceedProxyReturnValue();
    }
}

