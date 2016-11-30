package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusEntity;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeRemoveBricksVDSParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public class GetGlusterVolumeRemoveBricksStatusVDSCommand <P extends GlusterVolumeRemoveBricksVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    private GlusterVolumeTaskReturn result;

    public GetGlusterVolumeRemoveBricksStatusVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected Status getReturnStatus() {
        return result.getStatus();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        String volumeName = getParameters().getVolumeName();
        List<GlusterBrickEntity> bricksList = getParameters().getBricks();
        String[] brickNames = new String[bricksList.size()];
        for (int count = 0; count < bricksList.size(); count++) {
            brickNames[count] = bricksList.get(count).getQualifiedName();
        }

        result = getBroker().glusterVolumeRemoveBrickStatus(volumeName, brickNames);
        proceedProxyReturnValue();

        // Set the current engine time as status time
        GlusterVolumeTaskStatusEntity entity = result.getStatusDetails();
        entity.setStatusTime(new Date());
        setReturnValue(entity);
    }
}
