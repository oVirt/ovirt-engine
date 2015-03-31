package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;

public class GlusterTasksListVDSCommand<P extends VdsIdVDSCommandParametersBase> extends AbstractGlusterBrokerCommand<P> {

    private GlusterTasksListReturnForXmlRpc glusterTasksReturn;

    public GlusterTasksListVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        glusterTasksReturn = getBroker().glusterTasksList();

        proceedProxyReturnValue();
        if (getVDSReturnValue().getSucceeded()) {
            setReturnValue(glusterTasksReturn.getGlusterTasks());
        }
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return glusterTasksReturn.getXmlRpcStatus();
    }
}
