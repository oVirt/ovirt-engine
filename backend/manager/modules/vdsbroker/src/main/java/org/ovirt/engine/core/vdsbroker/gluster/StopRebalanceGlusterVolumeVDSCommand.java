package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusEntity;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeVDSParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public class StopRebalanceGlusterVolumeVDSCommand <P extends GlusterVolumeVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    private GlusterVolumeTaskReturn result;

    public StopRebalanceGlusterVolumeVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected Status getReturnStatus() {
        return result.getStatus();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        String volumeName = getParameters().getVolumeName();
        result = getBroker().glusterVolumeRebalanceStop(volumeName);
        proceedProxyReturnValue();

        // Set the current engine time as status time
        GlusterVolumeTaskStatusEntity entity = result.getStatusDetails();
        entity.setStatusTime(new Date());
        setReturnValue(entity);
    }
}
