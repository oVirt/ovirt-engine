package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetGlusterVolumeSnapshotScheduleByVolumeIdQuery<P extends IdQueryParameters> extends GlusterQueriesCommandBase<P> {
    public GetGlusterVolumeSnapshotScheduleByVolumeIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    public void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(glusterVolumeSnapshotScheduleDao.getByVolumeId(getParameters().getId()));
    }
}
