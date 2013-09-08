package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterHookVDSParameters;

public class AddGlusterHookVDSCommand<P extends GlusterHookVDSParameters> extends AbstractGlusterBrokerCommand<P> {

    public AddGlusterHookVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
         status =
                getBroker().glusterHookAdd(getParameters().getGlusterCommand(),
                        getParameters().getHookStage().toString(),
                        getParameters().getHookName(),
                        getParameters().getHookContent(),
                        getParameters().getChecksum(),
                        getParameters().getEnabled());

        proceedProxyReturnValue();
    }
}
