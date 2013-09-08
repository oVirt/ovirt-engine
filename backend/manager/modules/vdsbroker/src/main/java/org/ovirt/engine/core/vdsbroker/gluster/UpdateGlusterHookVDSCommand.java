package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterHookVDSParameters;

public class UpdateGlusterHookVDSCommand<P extends GlusterHookVDSParameters> extends AbstractGlusterBrokerCommand<P> {

    public UpdateGlusterHookVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
         status =
                getBroker().glusterHookUpdate(getParameters().getGlusterCommand(),
                        getParameters().getHookStage().toString(),
                        getParameters().getHookName(),
                        getParameters().getHookContent(),
                        getParameters().getChecksum());

        proceedProxyReturnValue();
    }
}
