package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusEntity;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeRemoveBricksVDSParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public class StopRemoveGlusterVolumeBricksVDSCommand<P extends GlusterVolumeRemoveBricksVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    private GlusterVolumeTaskReturn result;

    public StopRemoveGlusterVolumeBricksVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected Status getReturnStatus() {
        return result.getStatus();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        result = getBroker().glusterVolumeRemoveBricksStop(getParameters().getVolumeName(),
                getParameters().getBrickDirectories().toArray(new String[0]),
                getParameters().getReplicaCount());

        proceedProxyReturnValue();

        // Set the current engine time as status time
        GlusterVolumeTaskStatusEntity entity = result.getStatusDetails();
        entity.setStatusTime(new Date());
        setReturnValue(entity);
    }

}
