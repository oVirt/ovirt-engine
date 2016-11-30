package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public class GlusterTasksListVDSCommand<P extends VdsIdVDSCommandParametersBase> extends AbstractGlusterBrokerCommand<P> {

    private GlusterTasksListReturn glusterTasksReturn;

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
    protected Status getReturnStatus() {
        return glusterTasksReturn.getStatus();
    }
}
