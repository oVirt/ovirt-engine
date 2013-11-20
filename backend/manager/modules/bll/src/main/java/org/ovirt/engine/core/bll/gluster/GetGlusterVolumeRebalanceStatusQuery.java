package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusEntity;
import org.ovirt.engine.core.common.queries.gluster.GlusterVolumeQueriesParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeVDSParameters;

public class GetGlusterVolumeRebalanceStatusQuery<P extends GlusterVolumeQueriesParameters> extends GlusterAsyncTaskStatusQueryBase<P> {

    public GetGlusterVolumeRebalanceStatusQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected GlusterVolumeTaskStatusEntity fetchTaskStatusDetails() {
        VDSReturnValue returnValue =
                runVdsCommand(VDSCommandType.GetGlusterVolumeRebalanceStatus,
                        new GlusterVolumeVDSParameters(getUpServerId(clusterId), volume.getName()));

        return updateStatusEntity((GlusterVolumeTaskStatusEntity) returnValue.getReturnValue());
    }
}
