package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public class GlusterHooksListVDSCommand<P extends VdsIdVDSCommandParametersBase> extends AbstractGlusterBrokerCommand<P> {

    private GlusterHooksListReturn glusterHooks;

    public GlusterHooksListVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        glusterHooks = getBroker().glusterHooksList();

        proceedProxyReturnValue();
        if (getVDSReturnValue().getSucceeded()) {
            setReturnValue(glusterHooks.getGlusterHooks());
        }
    }

    @Override
    protected Status getReturnStatus() {
        return glusterHooks.getStatus();
    }

}
