package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRemoveBricksQueriesParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusEntity;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeRemoveBricksVDSParameters;

public class GetGlusterVolumeRemoveBricksStatusQuery<P extends GlusterVolumeRemoveBricksQueriesParameters> extends GlusterAsyncTaskStatusQueryBase<P> {

    public GetGlusterVolumeRemoveBricksStatusQuery(P params) {
        super(params);
    }

    @Override
    protected GlusterVolumeTaskStatusEntity fetchTaskStatusDetails() {
        VDSReturnValue returnValue =
                runVdsCommand(VDSCommandType.GetGlusterVolumeRemoveBricksStatus,
                        new GlusterVolumeRemoveBricksVDSParameters(getUpServerId(clusterId),
                                volume.getName(),
                                getParameters().getBricks(),
                                volume.getReplicaCount()));

        // update the status entity with required details
        return updateStatusEntity((GlusterVolumeTaskStatusEntity) returnValue.getReturnValue());
    }
}
