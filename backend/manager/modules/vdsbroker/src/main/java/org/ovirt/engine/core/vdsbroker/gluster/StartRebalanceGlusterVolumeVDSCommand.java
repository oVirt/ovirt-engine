package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeRebalanceVDSParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public class StartRebalanceGlusterVolumeVDSCommand<P extends GlusterVolumeRebalanceVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    private GlusterTaskInfoReturn glusterTaskReturn;
    private GlusterAsyncTask task;

    public StartRebalanceGlusterVolumeVDSCommand(P parameters) {
        super(parameters);
    }

    protected void executeVdsBrokerCommand() {
        glusterTaskReturn = getBroker().glusterVolumeRebalanceStart(getParameters().getVolumeName(),
                getParameters().isFixLayoutOnly(),
                getParameters().isForceAction());
        task = glusterTaskReturn.getGlusterTask();

        proceedProxyReturnValue();
        setReturnValue(task);
    }

    @Override
    protected Status getReturnStatus() {
        return glusterTaskReturn.getStatus();
    }

    @Override
    public Object getReturnValue() {
        return task;
    }



}
