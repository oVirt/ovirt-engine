package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetGlusterVolumeSnapshotScheduleByVolumeIdQuery<P extends IdQueryParameters> extends GlusterQueriesCommandBase<P> {
    public GetGlusterVolumeSnapshotScheduleByVolumeIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    public void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getGlusterVolumeSnapshotScheduleDao().getByVolumeId(getParameters().getId()));
    }
}
