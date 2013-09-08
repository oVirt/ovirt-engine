package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterHookVDSParameters;

public class DisableGlusterHookVDSCommand<P extends GlusterHookVDSParameters> extends AbstractGlusterBrokerCommand<P> {

    public DisableGlusterHookVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status =
                getBroker().glusterHookDisable(getParameters().getGlusterCommand(),
                        getParameters().getHookStage().toString(),
                        getParameters().getHookName());

        proceedProxyReturnValue();
    }
}
