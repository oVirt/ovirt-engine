package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterWebhookVDSParameters;


public class AddGlusterWebhookVDSCommand<P extends GlusterWebhookVDSParameters> extends AbstractGlusterBrokerCommand<P> {

    public AddGlusterWebhookVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().glusterWebhookAdd(getParameters().getWebhookUrl(), getParameters().getBearerToken());

        proceedProxyReturnValue();
    }
}

