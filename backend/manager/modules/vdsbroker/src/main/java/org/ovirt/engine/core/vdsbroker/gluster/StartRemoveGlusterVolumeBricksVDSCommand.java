package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeRemoveBricksVDSParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public class StartRemoveGlusterVolumeBricksVDSCommand<P extends GlusterVolumeRemoveBricksVDSParameters> extends AbstractGlusterBrokerCommand<P> {

    private GlusterTaskInfoReturn glusterTaskReturn;
    private GlusterAsyncTask task;

    public StartRemoveGlusterVolumeBricksVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected Status getReturnStatus() {
        return glusterTaskReturn.getStatus();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        glusterTaskReturn =
                getBroker().glusterVolumeRemoveBricksStart(getParameters().getVolumeName(),
                        getParameters().getBrickDirectories().toArray(new String[0]),
                        getParameters().getReplicaCount(),
                        getParameters().isForceRemove());
        task = glusterTaskReturn.getGlusterTask();
        proceedProxyReturnValue();
        setReturnValue(task);
    }

    @Override
    public Object getReturnValue() {
        return task;
    }
}
