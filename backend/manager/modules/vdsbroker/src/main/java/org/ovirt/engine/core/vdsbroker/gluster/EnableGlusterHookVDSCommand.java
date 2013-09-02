package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterHookVDSParameters;

public class EnableGlusterHookVDSCommand<P extends GlusterHookVDSParameters> extends AbstractGlusterBrokerCommand<P> {

    public EnableGlusterHookVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status =
                getBroker().glusterHookEnable(getParameters().getGlusterCommand(),
                        getParameters().getHookStage().toString(),
                        getParameters().getHookName());

        proceedProxyReturnValue();
    }
}
